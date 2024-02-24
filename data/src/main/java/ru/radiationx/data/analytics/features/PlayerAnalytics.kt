package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toErrorParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPlayerParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import toothpick.InjectConstructor

@InjectConstructor
class PlayerAnalytics(
    private val sender: AnalyticsSender,
) {

    private companion object {
        const val GROUPID = "Player"
    }

    fun open(from: String, playerType: AnalyticsPlayer, quality: AnalyticsQuality) {
        sender.send(
            AnalyticsConstants.player_open,
            from.toNavFromParam(),
            playerType.toPlayerParam(),
            quality.toQualityParam()
        )
    }

    fun playerError(error: Throwable) {
        sender.error(GROUPID, "playerError", error)
    }

    fun playerAudioCodecError(error: Throwable) {
        sender.error(GROUPID, "playerAudioCodecError", error)
    }

    fun playerAudioSinkError(error: Throwable) {
        sender.error(GROUPID, "playerAudioSinkError", error)
    }

    fun playerVideoCodecError(error: Throwable) {
        sender.error(GROUPID, "playerVideoCodecError", error)
    }

}