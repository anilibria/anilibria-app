package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toLinkParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class DonationInfraAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.donation_infra_open,
            from.toNavFromParam()
        )
    }

    fun linkClick(link: String) {
        sender.send(
            AnalyticsConstants.donation_infra_link_click,
            link.toLinkParam()
        )
    }

    fun telegramClick() {
        sender.send(AnalyticsConstants.donation_infra_telegram_click)
    }

}