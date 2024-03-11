package ru.radiationx.data.analytics.features

import android.net.Uri
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toPlayerParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.shared.ktx.asTimeSecString
import toothpick.InjectConstructor
import java.util.Date

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

    fun playerError(
        error: Throwable,
        data: ErrorData,
    ) {
        sender.error("playerError", data.message(), error)
    }

    fun playerAudioCodecError(error: Throwable, data: ErrorData) {
        sender.error("playerAudioCodecError", data.message(), error)
    }

    fun playerAudioSinkError(error: Throwable, data: ErrorData) {
        sender.error("playerAudioSinkError", data.message(), error)
    }

    fun playerVideoCodecError(error: Throwable, data: ErrorData) {
        sender.error("playerVideoCodecError", data.message(), error)
    }

    data class ErrorData(
        val episodeId: EpisodeId,
        val position: Long,
        val quality: AnalyticsQuality,
        val info: String?,
        val latestLoadUri: Uri?,
        val headerProtocol: String?,
        val headerHost: String?,
    ) {
        fun message(): String {
            val time = Date(position.coerceAtLeast(0L)).asTimeSecString()
            val params = listOf(
                "Episode" to "rid=${episodeId.releaseId.id}, eid=${episodeId.id}",
                "Pos" to time,
                "Quality" to quality.value,
                "Info" to info,
                "HeaderProtocol" to headerProtocol,
                "HeaderHost" to headerHost,
                "Uri" to latestLoadUri?.toString(),
            )
            return params.joinToString { "${it.first}='${it.second}'" }
        }
    }
}