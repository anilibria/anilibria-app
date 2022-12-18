package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
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

open class BasePlayerFragment : VideoSupportFragment() {


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

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initializePlayer()
        initializeRows()


        fadeCompleteListener = object : OnFadeCompleteListener() {

            override fun onFadeInComplete() {
                super.onFadeInComplete()
                // workaround for hiding controls when user click "enter"
                isControlsOverlayAutoHideEnabled = false
                isControlsOverlayAutoHideEnabled = true
            }
        }
    }


    override fun onPause() {
        super.onPause()
        playerGlue?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
    }

    protected open fun onCompletePlaying() {}
    protected open fun onPreparePlaying() {}

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

        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED -> onCompletePlaying()
                    Player.STATE_READY -> onPreparePlaying()
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_IDLE -> {
                    }
                }
            }
        })


        val playerAdapter = LeanbackPlayerAdapter(requireContext(), player, 500)

        val playerGlue = VideoPlayerGlue(requireContext(), playerAdapter).apply {
            host = VideoSupportFragmentGlueHost(this@BasePlayerFragment)
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
        player?.prepare(mediaSource, false, false)
    }

    private fun getMediaSource(url: String): MediaSource = Uri.parse(url).let {
        getMediaSourceFactory(it).createMediaSource(MediaItem.fromUri(it))
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