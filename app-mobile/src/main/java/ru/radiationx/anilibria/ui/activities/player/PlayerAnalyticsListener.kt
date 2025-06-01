package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.NetworkTypeObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ru.radiationx.anilibria.ui.activities.player.di.SharedPlayerData
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.mapper.toAnalyticsTransport
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.player.PlayerTransport
import timber.log.Timber
import java.io.IOException
import java.util.LinkedList
import javax.inject.Inject


class PlayerAnalyticsListener @Inject constructor(
    private val context: Context,
    private val playerAnalytics: PlayerAnalytics,
    private val buildConfig: SharedBuildConfig,
    private val sharedPlayerData: SharedPlayerData,
    private val preferences: PreferencesHolder,
) : AnalyticsListener {

    private val times = LinkedList<Long>()
    private val hostCounter = mutableMapOf<String, Int>()

    private val episodeId: EpisodeId
        get() = sharedPlayerData.episodeId.value

    private var latestLoadData: LoadData? = null

    var transport: PlayerTransport? = null

    @UnstableApi
    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean,
    ) {
        latestLoadData = loadEventInfo.toLoadData()
        Timber.e(error, "onLoadError")
    }

    @UnstableApi
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        latestLoadData = loadEventInfo.toLoadData()
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
    }

    @UnstableApi
    override fun onPlayerError(
        eventTime: AnalyticsListener.EventTime,
        error: PlaybackException,
    ) {
        val info = "${error.errorCode}, ${error.errorCodeName}"
        playerAnalytics.playerError(error, createErrorData(eventTime, info))
    }

    @UnstableApi
    override fun onAudioCodecError(
        eventTime: AnalyticsListener.EventTime,
        audioCodecError: Exception,
    ) {
        playerAnalytics.playerAudioCodecError(audioCodecError, createErrorData(eventTime, null))
    }

    @UnstableApi
    override fun onAudioSinkError(
        eventTime: AnalyticsListener.EventTime,
        audioSinkError: Exception,
    ) {
        playerAnalytics.playerAudioSinkError(audioSinkError, createErrorData(eventTime, null))
    }

    @UnstableApi
    override fun onVideoCodecError(
        eventTime: AnalyticsListener.EventTime,
        videoCodecError: Exception,
    ) {
        playerAnalytics.playerVideoCodecError(videoCodecError, createErrorData(eventTime, null))
    }

    @UnstableApi
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        latestLoadData = loadEventInfo.toLoadData()
        if (!buildConfig.debug) {
            return
        }
        times.add(loadEventInfo.loadDurationMs)
        val protocol = loadEventInfo.responseHeaders.let {
            (it["x-server-proto"] ?: it["X-Server-Proto"])?.firstOrNull()
        }.orEmpty()
        val frontHost = loadEventInfo.responseHeaders.let {
            (it["front-hostname"] ?: it["Front-Hostname"])?.firstOrNull()
        }.orEmpty()
        hostCounter[frontHost] = hostCounter.getOrPut(frontHost) { 0 } + 1


        val durationMs = loadEventInfo.loadDurationMs
        val average = times.average()
        val hostCount = hostCounter[frontHost]

        Timber
            .tag("onLoadCompleted")
            .d("loadEventInfo.loadDurationMs $durationMs, average $average, protocol $protocol, hostname $frontHost[$hostCount]")
    }

    @UnstableApi
    private fun createErrorData(
        eventTime: AnalyticsListener.EventTime,
        info: String?,
    ): PlayerAnalytics.ErrorData = PlayerAnalytics.ErrorData(
        episodeId = episodeId,
        position = eventTime.currentPlaybackPositionMs,
        quality = preferences.playerQuality.value.toAnalyticsQuality(),
        info = info,
        transport = transport?.toAnalyticsTransport(),
        duration = latestLoadData?.duration,
        bytes = latestLoadData?.bytes,
        networkType = getNetworkType(),
        latestLoadUri = latestLoadData?.uri,
        headerProtocol = latestLoadData?.protocol,
        headerHost = latestLoadData?.hostName,
        headerRange = latestLoadData?.range,
        headerLength = latestLoadData?.length,
        headerCacheZone = latestLoadData?.cacheZone,
        headerCacheStatus = latestLoadData?.cacheStatus
    )

    @UnstableApi
    private fun LoadEventInfo.toLoadData() = LoadData(
        uri = uri,
        protocol = getHeader("X-Server-Proto"),
        hostName = getHeader("Front-Hostname"),
        range = getHeader("Content-Range"),
        length = getHeader("Content-Length"),
        cacheZone = getHeader("X-Disk-Cache-Zone"),
        cacheStatus = getHeader("X-Cache-Status"),
        duration = loadDurationMs,
        bytes = bytesLoaded
    )

    @UnstableApi
    private fun LoadEventInfo.getHeader(name: String): String? = responseHeaders.let {
        (it[name.lowercase()] ?: it[name])?.firstOrNull()
    }

    @UnstableApi
    fun Int.toNetworkType(): PlayerAnalytics.NetworkType? = when (this) {
        C.NETWORK_TYPE_UNKNOWN -> PlayerAnalytics.NetworkType.Unknown
        C.NETWORK_TYPE_OFFLINE -> PlayerAnalytics.NetworkType.Offline
        C.NETWORK_TYPE_WIFI -> PlayerAnalytics.NetworkType.WiFi
        C.NETWORK_TYPE_2G -> PlayerAnalytics.NetworkType.Cell2g
        C.NETWORK_TYPE_3G -> PlayerAnalytics.NetworkType.Cell3g
        C.NETWORK_TYPE_4G -> PlayerAnalytics.NetworkType.Cell4g
        C.NETWORK_TYPE_5G_SA -> PlayerAnalytics.NetworkType.Cell5gSA
        C.NETWORK_TYPE_5G_NSA -> PlayerAnalytics.NetworkType.Cell5gNSA
        C.NETWORK_TYPE_CELLULAR_UNKNOWN -> PlayerAnalytics.NetworkType.CellUnknown
        C.NETWORK_TYPE_ETHERNET -> PlayerAnalytics.NetworkType.Ethernet
        C.NETWORK_TYPE_OTHER -> PlayerAnalytics.NetworkType.Other
        else -> null
    }

    @UnstableApi
    private fun getNetworkType(): PlayerAnalytics.NetworkType? {
        return try {
            NetworkTypeObserver.getInstance(context).networkType.toNetworkType()
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    private data class LoadData(
        val uri: Uri,
        val protocol: String?,
        val hostName: String?,
        val range: String?,
        val length: String?,
        val cacheZone: String?,
        val cacheStatus: String?,
        val duration: Long,
        val bytes: Long,
    )
}