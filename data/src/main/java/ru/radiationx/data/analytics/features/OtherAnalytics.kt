package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class OtherAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.other_open,
            from.toNavFromParam()
        )
    }

    fun loginClick() {
        sender.send(AnalyticsConstants.other_login_click)
    }

    fun logoutClick() {
        sender.send(AnalyticsConstants.other_logout_click)
    }

    fun profileClick() {
        sender.send(AnalyticsConstants.other_profile_click)
    }

    fun historyClick() {
        sender.send(AnalyticsConstants.other_history_click)
    }

    fun teamClick() {
        sender.send(AnalyticsConstants.other_team_click)
    }

    fun donateClick() {
        sender.send(AnalyticsConstants.other_donate_click)
    }

    fun authDeviceClick() {
        sender.send(AnalyticsConstants.other_auth_device_click)
    }

    fun settingsClick() {
        sender.send(AnalyticsConstants.other_settings_click)
    }

    fun linkClick(title: String) {
        sender.send(AnalyticsConstants.other_link_click)
    }

}