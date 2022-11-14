package ru.radiationx.anilibria.presentation.donation.detail

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailPresenter(
    router: Router,
    private val donationRepository: DonationRepository,
    private val detailAnalytics: DonationDetailAnalytics,
    private val yooMoneyAnalytics: DonationYooMoneyAnalytics,
    private val dialogAnalytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils
) : BasePresenter<DonationDetailView>(router) {

    private var currentData: DonationInfo? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        presenterScope.launch {
            runCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                Timber.e(it)
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
        systemUtils.externalLink(url)
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
                systemUtils.externalLink(buttonLink)
            }
        }
    }

}