package ru.radiationx.anilibria.presentation.donation.detail

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler
) : BasePresenter<DonationDetailView>(router) {

    private var currentData: DonationDetail? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribe({
                currentData = it
                viewState.showData(it)
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onLinkClick(url: String) {
        Utils.externalLink(url)
    }

    fun onPatreonClick() {
        currentData?.donateSupport?.btPatreon?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onYooMoneyClick() {
        viewState.openYooMoney()
    }

    fun onDonationAlertsClick() {
        currentData?.donateSupport?.btDonationAlerts?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onJoinTeamClick() {
        viewState.openJoinTeam()
    }

    fun onInfraClick() {
        viewState.openInfra()
    }
}