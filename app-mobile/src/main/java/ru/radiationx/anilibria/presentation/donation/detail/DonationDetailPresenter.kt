package ru.radiationx.anilibria.presentation.donation.detail

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.DonationInfraAnalytics
import ru.radiationx.data.analytics.features.DonationJoinTeamAnalytics
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val detailAnalytics: DonationDetailAnalytics,
    private val yooMoneyAnalytics: DonationYooMoneyAnalytics,
    private val joinTeamAnalytics: DonationJoinTeamAnalytics,
    private val infraAnalytics: DonationInfraAnalytics
) : BasePresenter<DonationDetailView>(router) {

    private var currentData: DonationDetail? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .requestUpdate()
            .subscribe({}, {
                it.printStackTrace()
            })
            .addToDisposable()
        donationRepository
            .observerDonationDetail()
            .subscribe({
                currentData = it
                viewState.showData(it)
            }, {
                it.printStackTrace()
            })
            .addToDisposable()
    }

    fun onLinkClick(url: String) {
        detailAnalytics.linkClick(url)
        Utils.externalLink(url)
    }

    fun onPatreonClick() {
        detailAnalytics.patreonClick()
        currentData?.donateSupport?.btPatreon?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onYooMoneyClick() {
        detailAnalytics.yoomoneyClick()
        yooMoneyAnalytics.open(AnalyticsConstants.screen_donation_detail)
        viewState.openYooMoney()
    }

    fun onDonationAlertsClick() {
        detailAnalytics.donationalertsClick()
        currentData?.donateSupport?.btDonationAlerts?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onJoinTeamClick() {
        detailAnalytics.jointeamClick()
        joinTeamAnalytics.open(AnalyticsConstants.screen_donation_detail)
        viewState.openJoinTeam()
    }

    fun onInfraClick() {
        detailAnalytics.infraClick()
        infraAnalytics.open(AnalyticsConstants.screen_donation_detail)
        viewState.openInfra()
    }
}