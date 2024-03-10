package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toPlayerParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import ru.radiationx.data.entity.domain.types.EpisodeId
import toothpick.InjectConstructor

@InjectConstructor
class PlayerAnalytics(
    private val sender: AnalyticsSender,
) {

    fun open(
        from: String,
        playerType: AnalyticsPlayer,
        quality: AnalyticsQuality,
        episodeId: EpisodeId,
    ) {
        sender.send(
            AnalyticsConstants.player_open,
            from.toNavFromParam(),
            playerType.toPlayerParam(),
            quality.toQualityParam(),
            episodeId.id.toParam("eid"),
            episodeId.releaseId.id.toParam("rid")
        )
    }

    fun playerError(error: Throwable, info: String, episodeId: EpisodeId) {
        sender.error("playerError", "Episode $episodeId, Info \"$info\"", error)
    }

    fun playerAudioCodecError(error: Throwable, episodeId: EpisodeId) {
        sender.error("playerAudioCodecError", "Episode $episodeId", error)
    }

    fun playerAudioSinkError(error: Throwable, episodeId: EpisodeId) {
        sender.error("playerAudioSinkError", "Episode $episodeId", error)
    }

    fun playerVideoCodecError(error: Throwable, episodeId: EpisodeId) {
        sender.error("playerVideoCodecError", "Episode $episodeId", error)
    }

}