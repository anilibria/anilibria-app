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

    private companion object {
        const val GROUPID = "Player"
    }

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

    fun playerError(error: Throwable, episodeId: EpisodeId) {
        sender.error(GROUPID, "playerError $episodeId", error)
    }

    fun playerAudioCodecError(error: Throwable, episodeId: EpisodeId) {
        sender.error(GROUPID, "playerAudioCodecError $episodeId", error)
    }

    fun playerAudioSinkError(error: Throwable, episodeId: EpisodeId) {
        sender.error(GROUPID, "playerAudioSinkError $episodeId", error)
    }

    fun playerVideoCodecError(error: Throwable, episodeId: EpisodeId) {
        sender.error(GROUPID, "playerVideoCodecError $episodeId", error)
    }

}