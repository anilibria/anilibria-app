package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.quill.get
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

open class BasePlayerFragment : VideoSupportFragment() {

    @UnstableApi
    protected var playerGlue: VideoPlayerGlue? = null
        private set

    protected var player: ExoPlayer? = null
        private set

    protected var skipsPart: PlayerSkipsPart? = null
        private set

    @SuppressLint("RestrictedApi")
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initializePlayer()
        initializeRows()

        skipsPart = PlayerSkipsPart(
            parent = view as FrameLayout,
            skipButtonText = getString(R.string.player_skip),
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            playerSkipsTimer = get<PreferencesHolder>().playerSkipsTimer,
            onSeek = {
                player?.seekTo(it)
            },
            onSkipShow = {
                isShowOrHideControlsOverlayOnUserInteraction = false
                hideControlsOverlay(false)
            },
            onSkipHide = {
                isShowOrHideControlsOverlayOnUserInteraction = true
            }
        )

        playerGlue?.playbackListener = object : VideoPlayerGlue.PlaybackListener {
            @UnstableApi
            override fun onUpdateProgress() {
                skipsPart?.update(player?.currentPosition ?: 0)
            }
        }

        fadeCompleteListener = object : OnFadeCompleteListener() {

            override fun onFadeInComplete() {
                super.onFadeInComplete()
                // workaround for hiding controls when user click "enter"
                isControlsOverlayAutoHideEnabled = false
                isControlsOverlayAutoHideEnabled = true
            }
        }
    }

    override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int) {
        if (videoWidth == 0 || videoHeight == 0) {
            return
        }
        super.onVideoSizeChanged(videoWidth, videoHeight)
    }

    override fun onPause() {
        super.onPause()
        playerGlue?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skipsPart = null
        playerGlue?.playbackListener = null
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
    }

    protected open fun onCompletePlaying() {}
    protected open fun onPreparePlaying() {}

    @UnstableApi
    private fun initializeRows() {
        val playerGlue = this.playerGlue ?: return
        val controlsRow = playerGlue.controlsRow ?: return

        val rowsPresenter = ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(controlsRow.javaClass, playerGlue.playbackRowPresenter)
        }
        val rowsAdapter = ArrayObjectAdapter(rowsPresenter).apply {
            add(controlsRow)
        }

        adapter = rowsAdapter
    }

    @UnstableApi
    private fun initializePlayer() {
        if (player != null) {
            throw RuntimeException("Player already initialized")
        }

        val dataSourceProvider = get<PlayerDataSourceProvider>()
        val dataSourceType = dataSourceProvider.get()
        val targetBufferBytes = PlayerBufferPolicy.targetBufferBytes(Runtime.getRuntime().maxMemory())
        val loadControl = DefaultLoadControl.Builder()
            .setTargetBufferBytes(targetBufferBytes)
            .setPrioritizeTimeOverSizeThresholds(false)
            .build()
        val playbackPaused = AtomicBoolean(true)
        val readGate = BufferReadGate(
            limitBytes = targetBufferBytes * 2,
            allocatedBytes = { loadControl.allocator.totalBytesAllocated },
            isPlaybackPaused = { playbackPaused.get() },
        )
        val upstreamFactory = DefaultDataSource.Factory(requireContext(), dataSourceType.factory)
        val dataSourceFactory = DataSource.Factory {
            HlsBufferingDataSource(upstreamFactory.createDataSource(), readGate)
        }
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext()).apply {
            setDataSourceFactory(dataSourceFactory)
            setLoadErrorHandlingPolicy(object : DefaultLoadErrorHandlingPolicy() {
                override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long =
                    if (loadErrorInfo.exception is BufferCapacityException) C.TIME_UNSET
                    else super.getRetryDelayMsFor(loadErrorInfo)
            })
        }
        val player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build()

        if (BuildConfig.DEBUG) {
            player.addAnalyticsListener(object : AnalyticsListener {
                override fun onLoadCompleted(
                    eventTime: AnalyticsListener.EventTime,
                    loadEventInfo: LoadEventInfo,
                    mediaLoadData: MediaLoadData,
                ) {
                    val runtime = Runtime.getRuntime()
                    Timber.tag("TvPlayerBuffer").d(
                        "positionMs=%d bufferedMs=%d allocatedBytes=%d heapUsedBytes=%d " +
                            "heapMaxBytes=%d targetBufferBytes=%d gateWaitCount=%d " +
                            "gatePeakAllocatedBytes=%d loadedBytes=%d video=%dx%d",
                        player.currentPosition,
                        player.totalBufferedDuration,
                        loadControl.allocator.totalBytesAllocated,
                        runtime.totalMemory() - runtime.freeMemory(),
                        runtime.maxMemory(),
                        targetBufferBytes,
                        readGate.waitCount.get(),
                        readGate.peakAllocatedBytes.get(),
                        loadEventInfo.bytesLoaded,
                        player.videoSize.width,
                        player.videoSize.height,
                    )
                }
            })
        }

        player.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                playbackPaused.set(!playWhenReady)
            }


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
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(url)), false)
        player?.prepare()
    }

}
