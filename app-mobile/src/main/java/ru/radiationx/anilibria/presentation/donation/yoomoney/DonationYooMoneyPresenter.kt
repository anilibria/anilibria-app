package ru.radiationx.anilibria.presentation.donation.yoomoney

import android.util.Log
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationYooMoneyPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler
) : BasePresenter<DonationYooMoneyView>(router) {

    private var currentState = DonationYooMoneyState()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribe({
                val yooMoneyInfo = it.donateSupport?.btYooMoney?.info
                val newState = currentState.copy(
                    data = yooMoneyInfo,
                    amountType = DonationYooMoneyState.AmountType.PRESET,
                    selectedAmount = yooMoneyInfo?.amounts?.defaultValue,
                    selectedTypeId = yooMoneyInfo?.paymentTypes?.selectedId
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
        val newState = currentState.copy(selectedTypeId = typeId)
        tryUpdateState(newState)
    }

    fun onAcceptClick() {
        val amount = currentState.getAmount()
        val typeId = currentState.selectedTypeId ?: return
        val form = currentState.data?.form ?: return
        viewState.setRefreshing(true)
        donationRepository
            .createYooMoneyPayLink(amount, typeId, form)
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
        val containsType = data?.paymentTypes?.items?.any { it.id == selectedTypeId } == true
        val isValidType = selectedTypeId != null && containsType
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
}