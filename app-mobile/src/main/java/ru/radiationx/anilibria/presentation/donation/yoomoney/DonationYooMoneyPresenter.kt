package ru.radiationx.anilibria.presentation.donation.yoomoney

import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationYooMoneyPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationYooMoneyView>(router) {

    private var currentState = DonationYooMoneyState()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val yooMoneyInfo = it.donateSupport?.btYooMoney?.info
                val newState = currentState.copy(data = yooMoneyInfo)
                tryUpdateState(newState)
            }, {
                it.printStackTrace()
            })
            .addToDisposable()
    }

    fun setSelectedAmount(value: Int) {
        val newState = currentState.copy(
            selectedAmount = value,
            amountType = DonationYooMoneyState.AmountType.PRESET
        )
        tryUpdateState(newState)
    }

    fun setCustomAmount(value: Int) {
        val newState = currentState.copy(
            customAmount = value,
            amountType = DonationYooMoneyState.AmountType.CUSTOM
        )
        tryUpdateState(newState)
    }

    fun setPaymentType(typeId: String) {
        val newState = currentState.copy(selectedTypeId = typeId)
        tryUpdateState(newState)
    }

    fun onAcceptClick() {
        viewState.closeView()
    }

    private fun DonationYooMoneyState.withValidation(): DonationYooMoneyState {
        val isValidAmount = when (amountType) {
            DonationYooMoneyState.AmountType.PRESET -> {
                (selectedAmount ?: 0) > 0
            }
            DonationYooMoneyState.AmountType.CUSTOM -> {
                (customAmount ?: 0) > 0
            }
        }
        val containsType = data?.paymentTypes?.items?.any { it.id == selectedTypeId } == true
        val isValidType = selectedTypeId != null && containsType
        return copy(acceptEnabled = isValidAmount && isValidType)
    }

    private fun tryUpdateState(newState: DonationYooMoneyState) {
        val withValidationState = newState.withValidation()
        if (currentState != withValidationState) {
            currentState = withValidationState
            viewState.showData(currentState)
        }
    }

}