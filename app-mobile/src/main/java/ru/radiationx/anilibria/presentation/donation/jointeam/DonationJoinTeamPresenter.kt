package ru.radiationx.anilibria.presentation.donation.jointeam

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.entity.app.donation.other.DonationJoinTeamInfo
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationJoinTeamPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler
) : BasePresenter<DonationJoinTeamView>(router) {

    private var currentData: DonationJoinTeamInfo? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationDetail()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
        currentData?.btVoicer?.link?.let {
            Utils.externalLink(it)
        }
    }

    fun onTelegramClick() {
        currentData?.btTelegram?.link?.let {
            Utils.externalLink(it)
        }
    }
}