package ru.radiationx.anilibria.screen.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dev.rx.tvtest.cust.CustomListRowPresenter
import ru.radiationx.anilibria.common.fragment.scoped.ScopedVideoFragment

open class BasePlayerFragment : ScopedVideoFragment() {


    protected var playerGlue: VideoPlayerGlue? = null
        private set

    protected var player: SimpleExoPlayer? = null
        private set

    private val dataSourceFactory by lazy {
        val userAgent = Util.getUserAgent(requireActivity(), "VideoPlayerGlue")
        DefaultDataSourceFactory(requireContext(), userAgent)
    }
    private val dashMediaSourceFactory by lazy { DashMediaSource.Factory(dataSourceFactory) }
    private val ssMediaSourceFactory by lazy { SsMediaSource.Factory(dataSourceFactory) }
    private val hlsMediaSourceFactory by lazy { HlsMediaSource.Factory(dataSourceFactory) }
    private val otherMediaSourceFactory by lazy { ProgressiveMediaSource.Factory(dataSourceFactory) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
        initializeRows()
    }

    override fun onStop() {
        super.onStop()
        playerGlue?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun initializeRows() {
        val playerGlue = this.playerGlue!!

        val rowsPresenter = ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(playerGlue.controlsRow.javaClass, playerGlue.playbackRowPresenter)
        }
        val rowsAdapter = ArrayObjectAdapter(rowsPresenter).apply {
            add(playerGlue.controlsRow)
        }

        adapter = rowsAdapter
    }

    private fun initializePlayer() {
        if (player != null) {
            throw RuntimeException("Player already initialized")
        }
        val bandwidthMeter = DefaultBandwidthMeter.Builder(requireContext()).build()
        val trackSelector = DefaultTrackSelector(requireContext(), AdaptiveTrackSelection.Factory())
        val player = SimpleExoPlayer.Builder(requireContext())
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .build()


        val playerAdapter = LeanbackPlayerAdapter(requireContext(), player, 16)

        val playerGlue = VideoPlayerGlue(requireContext(), playerAdapter).apply {
            host = VideoSupportFragmentGlueHost(this@BasePlayerFragment)
            playWhenPrepared()
        }

        this.player = player
        this.playerGlue = playerGlue
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    protected fun preparePlayer(url: String) {
        val mediaSource = getMediaSource(url)
        player?.prepare(mediaSource, true, true)
    }

    private fun getMediaSource(url: String): MediaSource = Uri.parse(url).let {
        getMediaSourceFactory(it).createMediaSource(it)
    }

    private fun getMediaSourceFactory(uri: Uri): MediaSourceFactory =
        when (val type = Util.inferContentType(uri)) {
            C.TYPE_DASH -> dashMediaSourceFactory
            C.TYPE_SS -> ssMediaSourceFactory
            C.TYPE_HLS -> hlsMediaSourceFactory
            C.TYPE_OTHER -> otherMediaSourceFactory
            else -> throw  IllegalStateException("Unsupported type: $type");
        }
}