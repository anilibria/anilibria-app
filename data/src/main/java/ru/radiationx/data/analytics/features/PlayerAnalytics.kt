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
import ru.radiationx.data.analytics.features.model.AnalyticsTransport
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.shared.ktx.asTimeSecString
import timber.log.Timber
import toothpick.InjectConstructor
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Date
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLKeyException
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLProtocolException

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

    fun handleEpisode(
        isNewIntent: Boolean,
        hasBundle: Boolean,
        episodeId: EpisodeId,
    ) {
        sender.send(
            AnalyticsConstants.player_handle_episode,
            isNewIntent.toParam("new"),
            hasBundle.toParam("bundle"),
            episodeId.id.toParam("eid"),
            episodeId.releaseId.id.toParam("rid")
        )
    }

    fun screenStart() {
        sender.send(AnalyticsConstants.player_screen_start)
    }

    fun screenStop() {
        sender.send(AnalyticsConstants.player_screen_stop)
    }

    fun pip() {
        sender.send(AnalyticsConstants.player_pip)
    }

    fun playerError(
        error: Throwable,
        data: ErrorData,
    ) {
        sendError("playerError", data.message(), error)
    }

    fun playerAudioCodecError(error: Throwable, data: ErrorData) {
        sendError("playerAudioCodecError", data.message(), error)
    }

    fun playerAudioSinkError(error: Throwable, data: ErrorData) {
        sendError("playerAudioSinkError", data.message(), error)
    }

    fun playerVideoCodecError(error: Throwable, data: ErrorData) {
        sendError("playerVideoCodecError", data.message(), error)
    }

    private fun sendError(
        groupId: String,
        message: String,
        throwable: Throwable,
    ) {
        try {
            val rootCause = throwable.findRootCause()
            val causeMessage = when (rootCause) {
                is SocketTimeoutException -> {
                    if (rootCause.message?.contains("SSL") == true) {
                        "Grouped ssl timeout"
                    } else {
                        "Grouped timeout"
                    }
                }

                is UnknownHostException -> "Grouped unknown host"

                is SSLProtocolException -> "Grouped ssl protocol"
                is SSLHandshakeException -> "Grouped ssl handshake"
                is SSLKeyException -> "Grouped ssl key"
                is SSLPeerUnverifiedException -> "Grouped ssl peer unverified"
                is SSLException -> "Grouped other ssl"
                else -> {
                    if (rootCause.message?.startsWith("Unexpected audio track timestamp discontinuity") == true) {
                        "Grouped Unexpected audio track timestamp discontinuity"
                    } else {
                        rootCause.message
                    }
                }
            }
            val groupName =
                "$groupId ${rootCause::class.simpleName} $causeMessage"
            sender.error(groupName, message, throwable)
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending error to appmetrica")
        }
    }

    private fun Throwable.findRootCause(): Throwable {
        var rootCause: Throwable? = this
        while (rootCause?.cause != null && rootCause.cause !== rootCause) {
            rootCause = rootCause.cause
        }
        return rootCause ?: this
    }


    data class ErrorData(
        val episodeId: EpisodeId,
        val position: Long,
        val quality: AnalyticsQuality,
        val info: String?,
        val transport: AnalyticsTransport?,
        val duration: Long?,
        val bytes: Long?,
        val networkType: NetworkType?,
        val latestLoadUri: Uri?,
        val headerProtocol: String?,
        val headerHost: String?,
        val headerRange: String?,
        val headerLength: String?,
        val headerCacheZone: String?,
        val headerCacheStatus: String?,
    )

    fun ErrorData.message(): String {
        val params = listOf(
            episodeId.toEpisodeParam(),
            position.toPositionParam(),
            "Qlt" to quality.value,
            "Drtn" to duration?.toString(),
            "Ntwk" to networkType?.value,
            "Info" to info,
            "Tprt" to transport?.value,
            "Bts" to bytes?.toString(),
            "H-Lngt" to headerLength,
            "H-Rng" to headerRange,
            "H-Ptcl" to headerProtocol,
            "H-C-St" to headerCacheStatus,
            "H-C-Zn" to headerCacheZone,
            "Uri" to latestLoadUri?.toString(),
        )
        return params.toParamsString()
    }

    private fun List<Pair<String, String?>>.toParamsString(): String {
        return joinToString { "${it.first}='${it.second}'" }
    }

    private fun EpisodeId.toEpisodeParam(): Pair<String, String> {
        return "Ep" to "rid=${releaseId.id}, eid=${id}"
    }

    private fun Long.toPositionParam(): Pair<String, String> {
        return "Pos" to Date(coerceAtLeast(0L)).asTimeSecString()
    }

    enum class NetworkType(val value: String) {
        Unknown("Unknown"),
        Offline("Offline"),
        WiFi("WiFi"),
        Cell2g("Cell2g"),
        Cell3g("Cell3g"),
        Cell4g("Cell4g"),
        Cell5gSA("Cell5gSA"),
        Cell5gNSA("Cell5gNSA"),
        CellUnknown("CellUnknown"),
        Ethernet("Ethernet"),
        Other("Other")
    }
}