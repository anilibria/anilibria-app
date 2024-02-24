package ru.radiationx.anilibria.ui.activities.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.PlayerAnalytics
import timber.log.Timber
import java.io.IOException
import java.util.LinkedList
import javax.inject.Inject


class PlayerAnalyticsListener @Inject constructor(
    private val playerAnalytics: PlayerAnalytics,
    private val buildConfig: SharedBuildConfig,
) : AnalyticsListener {

    private val times = LinkedList<Long>()
    private val hostCounter = mutableMapOf<String, Int>()

    @UnstableApi
    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean,
    ) {
        Timber.e("onLoadError", error)
    }

    @UnstableApi
    override fun onPlayerError(
        eventTime: AnalyticsListener.EventTime,
        error: PlaybackException,
    ) {
        playerAnalytics.playerError(error)
    }

    @UnstableApi
    override fun onAudioCodecError(
        eventTime: AnalyticsListener.EventTime,
        audioCodecError: Exception,
    ) {
        playerAnalytics.playerAudioCodecError(audioCodecError)
    }

    @UnstableApi
    override fun onAudioSinkError(
        eventTime: AnalyticsListener.EventTime,
        audioSinkError: Exception,
    ) {
        playerAnalytics.playerAudioSinkError(audioSinkError)
    }

    @UnstableApi
    override fun onVideoCodecError(
        eventTime: AnalyticsListener.EventTime,
        videoCodecError: Exception,
    ) {
        playerAnalytics.playerVideoCodecError(videoCodecError)
    }

    @UnstableApi
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
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
}