package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import toothpick.InjectConstructor

@InjectConstructor
class SettingsAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.settings_open,
            "from" to from
        )
    }

    fun notificationMainChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_notification_main_change,
            "value" to value.toString()
        )
    }

    fun notificationSystemChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_notification_system_change,
            "value" to value.toString()
        )
    }

    fun themeChange(value: String) {
        sender.send(
            AnalyticsConstants.settings_theme_change,
            "value" to value.toString()
        )
    }

    fun episodesOrderChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_episodes_order_change,
            "value" to value.toString()
        )
    }

    fun qualityClick() {
        sender.send(AnalyticsConstants.settings_quality_click)
    }

    fun qualityChange(value: AnalyticsQuality) {
        sender.send(
            AnalyticsConstants.settings_quality_change,
            "value" to value.value
        )
    }

    fun playerClick() {
        sender.send(AnalyticsConstants.settings_player_click)
    }

    fun playerChange(value: AnalyticsPlayer) {
        sender.send(
            AnalyticsConstants.settings_player_change,
            "value" to value.value
        )
    }

    fun checkUpdatesClick() {
        sender.send(AnalyticsConstants.settings_check_updates_click)
    }

    fun otherAppsClick() {
        sender.send(AnalyticsConstants.settings_other_apps_click)
    }

    fun fourPdaClick() {
        sender.send(AnalyticsConstants.settings_4pda_click)
    }

}