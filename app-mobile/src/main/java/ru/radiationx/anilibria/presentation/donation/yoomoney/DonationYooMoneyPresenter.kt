package ru.radiationx.anilibria.presentation.donation.yoomoney

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.analytics.features.model.AnalyticsDonationAmountType
import ru.radiationx.data.analytics.features.model.AnalyticsDonationPaymentType
import ru.radiationx.data.entity.app.donation.content_data.YooMoneyDialogResponse
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationYooMoneyPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler,
    private val analytics: DonationYooMoneyAnalytics
) : BasePresenter<DonationYooMoneyView>(router) {

    private var currentState = DonationYooMoneyState()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationInfo()
            .subscribe({
                val yooMoneyInfo = it.yooMoneyDialog
                val newState = currentState.copy(
                    data = yooMoneyInfo,
                    amountType = DonationYooMoneyState.AmountType.PRESET,
                    selectedAmount = yooMoneyInfo?.amounts?.defaultValue,
                    selectedPaymentTypeId = yooMoneyInfo?.paymentTypes?.selectedId
                )
                tryUpdateState(newState)
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun setSelectedAmount(value: Int?) {
        val newState = currentState.copy(
            selectedAmount = value,
            amountType = DonationYooMoneyState.AmountType.PRESET
        )
        tryUpdateState(newState)
    }

    fun setCustomAmount(value: Int?) {
        val newState = currentState.copy(
            customAmount = value,
            amountType = DonationYooMoneyState.AmountType.CUSTOM
        )
        tryUpdateState(newState)
    }

    fun setPaymentType(typeId: String?) {
        val newState = currentState.copy(selectedPaymentTypeId = typeId)
        tryUpdateState(newState)
    }

    fun submitHelpClickAnalytics() {
        analytics.helpClick()
    }

    fun onAcceptClick() {
        val amount = currentState.getAmount()
        val paymentTypeId = currentState.selectedPaymentTypeId ?: return
        val form = currentState.data?.form ?: return
        analytics.acceptClick(
            amount,
            currentState.amountType.toAnalytics(),
            paymentTypeId.toAnalyticsPaymentType()
        )
        viewState.setRefreshing(true)
        donationRepository
            .createYooMoneyPayLink(amount, paymentTypeId, form)
            .doFinally { viewState.setRefreshing(false) }
            .subscribe({
                Utils.externalLink(it)
                viewState.close()
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
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

    private fun tryUpdateState(newState: DonationYooMoneyState) {
        val withValidationState = newState.withValidation()
        if (currentState != withValidationState) {
            currentState = withValidationState
            viewState.showData(currentState)
        }
    }

    private fun DonationYooMoneyState.AmountType.toAnalytics() = when (this) {
        DonationYooMoneyState.AmountType.PRESET -> AnalyticsDonationAmountType.PRESET
        DonationYooMoneyState.AmountType.CUSTOM -> AnalyticsDonationAmountType.CUSTOM
    }

    private fun String.toAnalyticsPaymentType() = when (this) {
        YooMoneyDialogResponse.TYPE_ID_ACCOUNT -> AnalyticsDonationPaymentType.ACCOUNT
        YooMoneyDialogResponse.TYPE_ID_CARD -> AnalyticsDonationPaymentType.CARD
        YooMoneyDialogResponse.TYPE_ID_MOBILE -> AnalyticsDonationPaymentType.MOBILE
        else -> null
    }
}