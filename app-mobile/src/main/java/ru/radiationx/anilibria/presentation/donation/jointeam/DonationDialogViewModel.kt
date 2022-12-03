package ru.radiationx.anilibria.presentation.donation.jointeam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.mapNotNull
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

data class DonationDialogExtra(
    val tag: String
) : QuillExtra

@InjectConstructor
class DonationDialogViewModel(
    private val argExtra: DonationDialogExtra,
    private val donationRepository: DonationRepository,
    private val analytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils
) : ViewModel() {

    val state = donationRepository
        .observerDonationInfo()
        .mapNotNull { data ->
            data.contentDialogs.find { it.tag == argExtra.tag }
        }

    fun onLinkClick(url: String) {
        analytics.linkClick(argExtra.tag, url)
        systemUtils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        analytics.buttonClick(argExtra.tag, button.text)
        button.link?.also {
            systemUtils.externalLink(it)
        }
    }
}