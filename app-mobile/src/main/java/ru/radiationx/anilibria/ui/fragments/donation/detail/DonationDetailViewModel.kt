package ru.radiationx.anilibria.ui.fragments.donation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.app.donation.DonationRepository
import ru.radiationx.data.app.donation.models.DonationContentButton
import ru.radiationx.data.app.donation.models.DonationInfo
import ru.radiationx.data.common.Url
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import javax.inject.Inject

class DonationDetailViewModel @Inject constructor(
    private val router: Router,
    private val donationRepository: DonationRepository,
    private val detailAnalytics: DonationDetailAnalytics,
    private val yooMoneyAnalytics: DonationYooMoneyAnalytics,
    private val dialogAnalytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils
) : ViewModel() {

    private var currentData: DonationInfo? = null

    val state = donationRepository.observerDonationInfo()

    private val _yoomoneyEvent = EventFlow<Unit>()
    val yoomoneyEvent = _yoomoneyEvent.observe()

    private val _dialogEvent = EventFlow<String>()
    val dialogEvent = _dialogEvent.observe()

    init {
        state.onEach {
            currentData = it
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            coRunCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onBackPressed() {
        router.exit()
    }

    fun onLinkClick(url: Url.Absolute) {
        detailAnalytics.linkClick(url.raw)
        systemUtils.open(url)
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
                _yoomoneyEvent.set(Unit)
            }

            dialog != null -> {
                dialogAnalytics.open(AnalyticsConstants.screen_donation_detail, dialog.tag)
                _dialogEvent.set(dialog.tag)
            }

            buttonLink != null -> {
                systemUtils.open(buttonLink)
            }
        }
    }

}