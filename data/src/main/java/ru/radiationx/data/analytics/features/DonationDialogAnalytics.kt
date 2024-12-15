package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toLinkParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import javax.inject.Inject

class DonationDialogAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String, tag: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_open,
            from.toNavFromParam(),
            tag.toParam("tag")
        )
    }

    fun linkClick(fromTag: String, link: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_link_click,
            link.toLinkParam(),
            fromTag.toParam("tag")
        )
    }

    fun buttonClick(fromTag: String, buttonText: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_button_click,
            buttonText.toParam("text"),
            fromTag.toParam("tag")
        )
    }
}