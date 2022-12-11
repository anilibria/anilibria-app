package ru.radiationx.anilibria.ui.fragments.donation.yoomoney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.analytics.features.model.AnalyticsDonationAmountType
import ru.radiationx.data.analytics.features.model.AnalyticsDonationPaymentType
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class DonationYooMoneyViewModel(
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler,
    private val analytics: DonationYooMoneyAnalytics,
    private val systemUtils: SystemUtils
) : ViewModel() {

    private val _state = MutableStateFlow(DonationYooMoneyState())
    val state = _state.asStateFlow()

    private val _closeEvent = EventFlow<Unit>()
    val closeEvent = _closeEvent.observe()

    init {
        donationRepository
            .observerDonationInfo()
            .onEach { data ->
                val yooMoneyInfo = data.yooMoneyDialog
                tryUpdateState {
                    it.copy(
                        data = yooMoneyInfo,
                        amountType = DonationYooMoneyState.AmountType.PRESET,
                        selectedAmount = yooMoneyInfo?.amounts?.defaultValue,
                        selectedPaymentTypeId = yooMoneyInfo?.paymentTypes?.selectedId
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setSelectedAmount(value: Int?) {
        tryUpdateState {
            it.copy(
                selectedAmount = value,
                amountType = DonationYooMoneyState.AmountType.PRESET
            )
        }
    }

    fun setCustomAmount(value: Int?) {
        tryUpdateState {
            it.copy(
                customAmount = value,
                amountType = DonationYooMoneyState.AmountType.CUSTOM
            )
        }
    }

    fun setPaymentType(typeId: String?) {
        tryUpdateState {
            it.copy(selectedPaymentTypeId = typeId)
        }
    }

    fun submitHelpClickAnalytics() {
        analytics.helpClick()
    }

    fun onAcceptClick() {
        val amount = _state.value.getAmount()
        val paymentTypeId = _state.value.selectedPaymentTypeId ?: return
        val form = _state.value.data?.form ?: return
        analytics.acceptClick(
            amount,
            _state.value.amountType.toAnalytics(),
            paymentTypeId.toAnalyticsPaymentType()
        )
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            coRunCatching {
                donationRepository.createYooMoneyPayLink(amount, paymentTypeId, form)
            }.onSuccess {
                systemUtils.externalLink(it)
                _closeEvent.set(Unit)
            }.onFailure {
                errorHandler.handle(it)
            }
            _state.update { it.copy(sending = false) }
        }
    }

    private fun DonationYooMoneyState.withValidation(): DonationYooMoneyState {
        val isValidAmount = getAmount() > 0
        val containsType = data?.paymentTypes?.items?.any { it.id == selectedPaymentTypeId } == true
        val isValidType = selectedPaymentTypeId != null && containsType
        return copy(acceptEnabled = isValidAmount && isValidType)
    }

    private fun DonationYooMoneyState.getAmount(): Int = when (amountType) {
        DonationYooMoneyState.AmountType.PRESET -> selectedAmount ?: 0
        DonationYooMoneyState.AmountType.CUSTOM -> customAmount ?: 0
    }

    private fun tryUpdateState(block: (DonationYooMoneyState) -> DonationYooMoneyState) {
        _state.update {
            val newState = block.invoke(_state.value)
            newState.withValidation()
        }
    }

    private fun DonationYooMoneyState.AmountType.toAnalytics() = when (this) {
        DonationYooMoneyState.AmountType.PRESET -> AnalyticsDonationAmountType.PRESET
        DonationYooMoneyState.AmountType.CUSTOM -> AnalyticsDonationAmountType.CUSTOM
    }

    private fun String.toAnalyticsPaymentType() = when (this) {
        YooMoneyDialog.TYPE_ID_ACCOUNT -> AnalyticsDonationPaymentType.ACCOUNT
        YooMoneyDialog.TYPE_ID_CARD -> AnalyticsDonationPaymentType.CARD
        YooMoneyDialog.TYPE_ID_MOBILE -> AnalyticsDonationPaymentType.MOBILE
        else -> null
    }
}