package ru.radiationx.data.analytics.features

import androidx.annotation.FloatRange
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.*
import ru.radiationx.data.analytics.features.model.*
import toothpick.InjectConstructor

@InjectConstructor
class PlayerAnalytics(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_SPEED = "speed"
        const val PARAM_REWIND_TIME = "rewind_time"
        const val PARAM_SEEK_PERCENT = "seek_percent"
        const val PARAM_HOST = "host"
    }

    private fun Long?.toRewindTimeParam() = this.toTimeParam(PARAM_REWIND_TIME)

    private fun Float?.toSeekPercentParam() = this?.times(100)?.toInt().toParam(PARAM_SEEK_PERCENT)

    fun open(from: String, playerType: AnalyticsPlayer, quality: AnalyticsQuality) {
        sender.send(
            AnalyticsConstants.player_open,
            from.toNavFromParam(),
            playerType.toPlayerParam(),
            quality.toQualityParam()
        )
    }

    fun timeToStart(host: String, quality: AnalyticsQuality, timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.player_time_to_start,
            host.toParam(PARAM_HOST),
            quality.toQualityParam(),
            timeInMillis.toTimeParam()
        )
    }

    fun error(error: Throwable) {
        sender.send(
            AnalyticsConstants.player_error,
            error.toErrorParam()
        )
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.player_use_time,
            timeInMillis.toTimeParam()
        )
    }

    fun playClick() {
        sender.send(AnalyticsConstants.player_play_click)
    }

    fun pauseClick() {
        sender.send(AnalyticsConstants.player_pause_click)
    }

    fun prevClick(@FloatRange(from = 0.0, to = 1.0) fromSeekPercent: Float) {
        sender.send(
            AnalyticsConstants.player_prev_click,
            fromSeekPercent.toSeekPercentParam()
        )
    }

    fun nextClick(@FloatRange(from = 0.0, to = 1.0) fromSeekPercent: Float) {
        sender.send(
            AnalyticsConstants.player_next_click,
            fromSeekPercent.toSeekPercentParam()
        )
    }

    fun rewindSeek(
        @FloatRange(from = 0.0, to = 1.0) fromSeekPercent: Float,
        rewindTimeInMillis: Long
    ) {
        sender.send(
            AnalyticsConstants.player_rewind_seek,
            fromSeekPercent.toSeekPercentParam(),
            rewindTimeInMillis.toRewindTimeParam()
        )
    }

    fun fullScreen(@FloatRange(from = 0.0, to = 1.0) fromSeekPercent: Float) {
        sender.send(
            AnalyticsConstants.player_fullscreen,
            fromSeekPercent.toSeekPercentParam()
        )
    }

    fun pip(@FloatRange(from = 0.0, to = 1.0) fromSeekPercent: Float) {
        sender.send(
            AnalyticsConstants.player_pip,
            fromSeekPercent.toSeekPercentParam()
        )
    }

    fun settingsClick() {
        sender.send(AnalyticsConstants.player_settings_click)
    }

    fun settingsQualityClick() {
        sender.send(AnalyticsConstants.player_settings_quality_click)
    }

    fun settingsSpeedClick() {
        sender.send(AnalyticsConstants.player_settings_speed_click)
    }

    fun settingsScaleClick() {
        sender.send(AnalyticsConstants.player_settings_scale_click)
    }

    fun settingsPipClick() {
        sender.send(AnalyticsConstants.player_settings_pip_click)
    }

    fun settingsQualityChange(quality: AnalyticsQuality) {
        sender.send(
            AnalyticsConstants.player_settings_quality_change,
            quality.toQualityParam()
        )
    }

    fun settingsSpeedChange(speed: Float) {
        sender.send(
            AnalyticsConstants.player_settings_speed_change,
            speed.toParam(PARAM_SPEED)
        )
    }

    fun settingsScaleChange(scale: AnalyticsVideoScale) {
        sender.send(
            AnalyticsConstants.player_settings_scale_change,
            scale.toScaleParam()
        )
    }

    fun settingsPipChange(pip: AnalyticsPip) {
        sender.send(
            AnalyticsConstants.player_settings_pip_change,
            pip.toPipParam()
        )
    }

    fun episodesFinish() {
        sender.send(AnalyticsConstants.player_episodes_finish)
    }

    fun episodesFinishAction(action: AnalyticsEpisodeFinishAction) {
        sender.send(
            AnalyticsConstants.player_episodes_finish_action,
            action.toActionParam()
        )
    }

    fun seasonFinish() {
        sender.send(AnalyticsConstants.player_season_finish)
    }

    fun seasonFinishAction(action: AnalyticsSeasonFinishAction) {
        sender.send(
            AnalyticsConstants.player_season_finish_action,
            action.toActionParam()
        )
    }
}