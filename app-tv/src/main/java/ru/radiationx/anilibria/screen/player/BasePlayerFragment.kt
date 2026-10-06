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
import androidx.leanback.widget.PlaybackSeekUi
import androidx.leanback.widget.PlaybackSeekDataProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.quill.get

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
            onPlayPause = { player?.let { it.playWhenReady = !it.playWhenReady } },
            onSkipShow = {
                isShowOrHideControlsOverlayOnUserInteraction = false
                isControlsOverlayAutoHideEnabled = false
            },
            onSkipHide = {
                isShowOrHideControlsOverlayOnUserInteraction = true
                isControlsOverlayAutoHideEnabled = skipsPart?.isSeeking != true && player?.isPlaying == true
                if (skipsPart?.isSeeking == true) showControlsOverlay(false)
            }
        )

        playerGlue?.isControlsOverlayAutoHideEnabled = false
        playerGlue?.playbackListener = object : VideoPlayerGlue.PlaybackListener {
            @UnstableApi
            override fun onUpdateProgress() {
                updateSkipPlaybackState()
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
        skipsPart?.dispose()
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
        val dataSourceFactory = DefaultDataSource.Factory(requireContext(), dataSourceType.factory)
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext()).apply {
            setDataSourceFactory(dataSourceFactory)
        }
        val player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {

            override fun onRenderedFirstFrame() {
                skipsPart?.onFrameRendered(this@BasePlayerFragment.player?.currentPosition ?: 0)
                updateSkipPlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateSkipPlaybackState()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK ||
                    reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
                ) {
                    skipsPart?.onFramePending(newPosition.positionMs, oldPosition.positionMs != newPosition.positionMs || oldPosition.mediaItemIndex != newPosition.mediaItemIndex)
                    updateSkipPlaybackState()
                }
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
                updateSkipPlaybackState()
            }
        })


        val playerAdapter = LeanbackPlayerAdapter(requireContext(), player, 500)

        val playerGlue = VideoPlayerGlue(requireContext(), playerAdapter).apply {
            host = object : VideoSupportFragmentGlueHost(this@BasePlayerFragment) {
                override fun setPlaybackSeekUiClient(client: PlaybackSeekUi.Client?) {
                    super.setPlaybackSeekUiClient(client?.let { delegate ->
                        object : PlaybackSeekUi.Client() {
                            override fun isSeekEnabled(): Boolean = delegate.isSeekEnabled

                            override fun getPlaybackSeekDataProvider(): PlaybackSeekDataProvider? =
                                delegate.playbackSeekDataProvider

                            override fun onSeekStarted() {
                                skipsPart?.onSeekStarted()
                                delegate.onSeekStarted()
                            }

                            override fun onSeekPositionChanged(pos: Long) {
                                delegate.onSeekPositionChanged(pos)
                            }

                            override fun onSeekFinished(cancelled: Boolean) {
                                delegate.onSeekFinished(cancelled)
                                skipsPart?.onSeekFinished(
                                    this@BasePlayerFragment.player?.currentPosition ?: 0,
                                    cancelled,
                                )
                            }
                        }
                    })
                }
            }
        }

        this.player = player
        this.playerGlue = playerGlue
    }

    @OptIn(UnstableApi::class)
    private fun updateSkipPlaybackState() {
        val player = player ?: return
        skipsPart?.onPlaybackChanged(player.playbackState == Player.STATE_READY, player.isPlaying, player.currentPosition)
        val autoHide = isShowOrHideControlsOverlayOnUserInteraction &&
            skipsPart?.isSeeking != true && player.isPlaying
        if (isControlsOverlayAutoHideEnabled != autoHide) isControlsOverlayAutoHideEnabled = autoHide
        if (!player.playWhenReady && isShowOrHideControlsOverlayOnUserInteraction) showControlsOverlay(false)
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    protected fun preparePlayer(url: String) {
        skipsPart?.onVideoLoading()
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(url)), false)
        player?.prepare()
    }

}
