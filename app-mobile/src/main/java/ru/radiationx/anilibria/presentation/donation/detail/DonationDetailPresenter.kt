package ru.radiationx.anilibria.presentation.donation.detail

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    val donationRepository: DonationRepository
) : BasePresenter<DonationDetailView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.showData(it)
            }, {
                it.printStackTrace()
            })
            .addToDisposable()
    }

    fun onPatreonClick() {

    }

    fun onYooMoneyClick() {

    }

    fun onDonationAlertsClick() {

    }

    fun onJoinTeamClick() {

    }

    fun onInfraClick() {

    }
}