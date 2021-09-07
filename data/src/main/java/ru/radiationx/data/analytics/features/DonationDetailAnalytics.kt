package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toLinkParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class DonationDetailAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.donation_detail_open,
            from.toNavFromParam()
        )
    }

    fun linkClick(link: String) {
        sender.send(
            AnalyticsConstants.donation_detail_link_click,
            link.toLinkParam()
        )
    }

    fun patreonClick() {
        sender.send(AnalyticsConstants.donation_detail_patreon_click)
    }

    fun yoomoneyClick() {
        sender.send(AnalyticsConstants.donation_detail_yoomoney_click)
    }

    fun donationalertsClick() {
        sender.send(AnalyticsConstants.donation_detail_donationalerts_click)
    }

    fun jointeamClick() {
        sender.send(AnalyticsConstants.donation_detail_jointeam_click)
    }

    fun infraClick() {
        sender.send(AnalyticsConstants.donation_detail_infra_click)
    }
}