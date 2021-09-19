package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toLinkParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import toothpick.InjectConstructor

@InjectConstructor
class DonationDialogAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_open,
            from.toNavFromParam()
        )
    }

    fun linkClick(link: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_link_click,
            link.toLinkParam()
        )
    }

    fun buttonClick(buttonText: String) {
        sender.send(
            AnalyticsConstants.donation_dialog_button_click,
            buttonText.toParam("text")
        )
    }
}