package ru.radiationx.anilibria.presentation.donation.jointeam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.mapNotNull
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationDialog
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DonationDialogViewModel(
    router: Router,
    private val donationRepository: DonationRepository,
    private val analytics: DonationDialogAnalytics,
    private val systemUtils: SystemUtils
) : ViewModel() {

    private var currentData: DonationDialog? = null

    var argTag: String? = null

    val state = donationRepository
        .observerDonationInfo()
        .mapNotNull { data ->
            data.contentDialogs.find { it.tag == argTag }
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