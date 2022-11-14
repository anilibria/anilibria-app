package ru.radiationx.anilibria.presentation.donation.jointeam

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationDialog
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDialogPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val analytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils
) : BasePresenter<DonationJoinTeamView>(router) {

    private var currentData: DonationDialog? = null

    var argTag: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        donationRepository
            .observerDonationInfo()
            .onEach {
                val donationDialog = it.contentDialogs.find { it.tag == argTag }
                if (donationDialog != null) {
                    currentData = donationDialog
                    viewState.showData(donationDialog)
                }
            }
            .launchIn(presenterScope)
    }

    fun onLinkClick(url: String) {
        analytics.linkClick(argTag.toString(), url)
        systemUtils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        analytics.buttonClick(argTag.toString(), button.text)
        button.link?.also {
            systemUtils.externalLink(it)
        }
    }
}