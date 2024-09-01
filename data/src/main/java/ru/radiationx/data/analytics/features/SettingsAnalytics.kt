package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toPlayerParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.extensions.toThemeParam
import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import toothpick.InjectConstructor

@InjectConstructor
class SettingsAnalytics(
    private val sender: AnalyticsSender
) {

    private companion object{
        const val PARAM_VALUE = "value"
    }

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.settings_open,
            from.toNavFromParam()
        )
    }

    fun notificationMainChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_notification_main_change,
            value.toParam(PARAM_VALUE)
        )
    }

    fun notificationSystemChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_notification_system_change,
            value.toParam(PARAM_VALUE)
        )
    }

    fun themeChange(theme: AnalyticsAppTheme) {
        sender.send(
            AnalyticsConstants.settings_theme_change,
            theme.toThemeParam()
        )
    }

    fun episodesOrderChange(value: Boolean) {
        sender.send(
            AnalyticsConstants.settings_reverse_order_change,
            value.toParam(PARAM_VALUE)
        )
    }

    fun qualityClick() {
        sender.send(AnalyticsConstants.settings_quality_click)
    }

    fun qualityChange(quality: AnalyticsQuality) {
        sender.send(
            AnalyticsConstants.settings_quality_change,
            quality.toQualityParam()
        )
    }

    fun playerClick() {
        sender.send(AnalyticsConstants.settings_player_click)
    }

    fun playerChange(player: AnalyticsPlayer) {
        sender.send(
            AnalyticsConstants.settings_player_change,
            player.toPlayerParam()
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