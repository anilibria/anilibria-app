package ru.radiationx.anilibria.presentation.donation.detail

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.repository.DonationRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val detailAnalytics: DonationDetailAnalytics,
    private val yooMoneyAnalytics: DonationYooMoneyAnalytics,
    private val dialogAnalytics: DonationDialogAnalytics
) : BasePresenter<DonationDetailView>(router) {

    private var currentData: DonationInfo? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        presenterScope.launch {
            runCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                it.printStackTrace()
            }
        }
        donationRepository
            .observerDonationInfo()
            .onEach {
                currentData = it
                viewState.showData(it)
            }
            .launchIn(presenterScope)
    }

    fun onLinkClick(url: String) {
        detailAnalytics.linkClick(url)
        Utils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        detailAnalytics.buttonClick(button.text)
        val info = currentData ?: return
        val buttonTag = button.tag
        val buttonLink = button.link

        val dialog = buttonTag?.let { tag -> info.contentDialogs.find { it.tag == tag } }
        val yoomoneyDialog = buttonTag
            ?.takeIf { it == DonationInfo.YOOMONEY_TAG }
            ?.let { info.yooMoneyDialog }

        when {
            yoomoneyDialog != null -> {
                yooMoneyAnalytics.open(AnalyticsConstants.screen_donation_detail)
                viewState.openYooMoney()
            }
            dialog != null -> {
                dialogAnalytics.open(AnalyticsConstants.screen_donation_detail, dialog.tag)
                viewState.openContentDialog(dialog.tag)
            }
            buttonLink != null -> {
                Utils.externalLink(buttonLink)
            }
        }
    }

}