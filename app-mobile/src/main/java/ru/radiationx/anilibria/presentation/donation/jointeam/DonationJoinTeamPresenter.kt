package ru.radiationx.anilibria.presentation.donation.jointeam

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.features.DonationJoinTeamAnalytics
import ru.radiationx.data.entity.app.donation.other.DonationJoinTeamInfo
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationJoinTeamPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler,
    private val analytics: DonationJoinTeamAnalytics
) : BasePresenter<DonationJoinTeamView>(router) {

    private var currentData: DonationJoinTeamInfo? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribe({
                val joinTeamInfo = it.otherSupport?.btJoinTeam?.info
                if (joinTeamInfo != null) {
                    currentData = joinTeamInfo
                    viewState.showData(joinTeamInfo)
                }
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onNoticeClick() {
        analytics.noticeClick()
        currentData?.btVoicer?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onTelegramClick() {
        analytics.telegramClick()
        currentData?.btTelegram?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onLinkClick(url: String) {
        analytics.linkClick(url)
        Utils.externalLink(url)
    }
}