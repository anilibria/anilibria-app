package ru.radiationx.anilibria.presentation.donation.jointeam

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationDialog
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDialogPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val errorHandler: ErrorHandler,
    private val analytics: DonationDialogAnalytics
) : BasePresenter<DonationJoinTeamView>(router) {

    private var currentData: DonationDialog? = null

    var argTag: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationInfo()
            .subscribe({
                val donationDialog = it.contentDialogs.find { it.tag == argTag }
                if (donationDialog != null) {
                    currentData = donationDialog
                    viewState.showData(donationDialog)
                }
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onLinkClick(url: String) {
        analytics.linkClick(url)
        Utils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        analytics.buttonClick(button.text)
        button.link?.also {
            Utils.externalLink(it)
        }
    }
}