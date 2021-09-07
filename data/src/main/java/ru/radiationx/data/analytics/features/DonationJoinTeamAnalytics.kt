package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toLinkParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class DonationJoinTeamAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.donation_jointeam_open,
            from.toNavFromParam()
        )
    }

    fun linkClick(link: String) {
        sender.send(
            AnalyticsConstants.donation_jointeam_link_click,
            link.toLinkParam()
        )
    }

    fun noticeClick() {
        sender.send(AnalyticsConstants.donation_jointeam_notice_click)
    }

    fun telegramClick() {
        sender.send(AnalyticsConstants.donation_jointeam_telegram_click)
    }

}