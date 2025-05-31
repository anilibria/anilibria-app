package ru.radiationx.anilibria.ui.fragments.donation.jointeam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.mapNotNull
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

data class DonationDialogExtra(
    val tag: String,
) : QuillExtra

class DonationDialogViewModel @Inject constructor(
    private val argExtra: DonationDialogExtra,
    donationRepository: DonationRepository,
    private val analytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    val state = donationRepository
        .observerDonationInfo()
        .mapNotNull { data ->
            data.contentDialogs.find { it.tag == argExtra.tag }
        }

    fun onLinkClick(url: Url.Absolute) {
        analytics.linkClick(argExtra.tag, url.raw)
        systemUtils.open(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        analytics.buttonClick(argExtra.tag, button.text)
        button.link?.also {
            systemUtils.open(it)
        }
    }
}