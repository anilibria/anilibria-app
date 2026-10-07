package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
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

    private var isPlayPausePressed = false
    private var upNavigationFocus: View? = null

    @SuppressLint("RestrictedApi")
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isControlsOverlayAutoHideEnabled = true
        isShowOrHideControlsOverlayOnUserInteraction = true

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initializePlayer()
        initializeRows()

        // Attach after the glue host, which installs its own key listener during initialization.
        setOnKeyInterceptListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (!isPlayPausePressed && event.repeatCount == 0) {
                            if (playerGlue?.isPlaying == true) {
                                playerGlue?.pause()
                            } else {
                                playerGlue?.play()
                            }
                        }
                        isPlayPausePressed = true
                    }
                    KeyEvent.ACTION_UP -> isPlayPausePressed = false
                }
                true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (event.repeatCount == 0) {
                            val focusedView = view.findFocus()
                            upNavigationFocus = focusedView?.takeIf {
                                isControlsOverlayVisible &&
                                    isShowOrHideControlsOverlayOnUserInteraction &&
                                    // Up is intentionally consumed while the seek bar is seeking.
                                    it.id != androidx.leanback.R.id.playback_progress
                            }
                        }
                        false
                    }
                    KeyEvent.ACTION_UP -> {
                        val previousFocus = upNavigationFocus
                        upNavigationFocus = null
                        if (previousFocus != null && previousFocus === view.findFocus() &&
                            isControlsOverlayVisible && isShowOrHideControlsOverlayOnUserInteraction
                        ) {
                            // Leanback shows the overlay on key-down events, so hide on release.
                            hideControlsOverlay(true)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            } else {
                playerGlue?.onKey(view, keyCode, event) == true
            }
        }

        skipsPart = PlayerSkipsPart(
            parent = view as FrameLayout,
            skipButtonText = getString(R.string.player_skip),
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            playerSkipsTimer = get<PreferencesHolder>().playerSkipsTimer,
            onSeek = { position -> player?.seekTo(position) },
            onSkipShow = {
                isShowOrHideControlsOverlayOnUserInteraction = false
                isControlsOverlayAutoHideEnabled = false
                hideControlsOverlay(false)
            },
            onSkipHide = {
                isShowOrHideControlsOverlayOnUserInteraction = true
                isControlsOverlayAutoHideEnabled = playerGlue?.isPlaying == true
                if (playerGlue?.isPlaying == true) {
                    hideControlsOverlay(false)
                } else {
                    showControlsOverlay(false)
                }
            }
        )

        playerGlue?.playbackListener = object : VideoPlayerGlue.PlaybackListener {
            override fun onUpdateProgress() {
                skipsPart?.update(player?.currentPosition ?: 0)
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
        isPlayPausePressed = false
        upNavigationFocus = null
        super.onPause()
        playerGlue?.pause()
    }

    @OptIn(UnstableApi::class)
    override fun onDestroyView() {
        super.onDestroyView()
        upNavigationFocus = null
        skipsPart = null
        playerGlue?.playbackListener = null
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
        playerGlue = null
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
        check(player == null) { "Player already initialized" }

        val dataSourceProvider = get<PlayerDataSourceProvider>()
        val dataSourceType = dataSourceProvider.get()
        val dataSourceFactory = DefaultDataSource.Factory(requireContext(), dataSourceType.factory)
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext()).apply {
            setDataSourceFactory(dataSourceFactory)
        }
        player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            Player.STATE_ENDED -> onCompletePlaying()
                            Player.STATE_READY -> onPreparePlaying()
                            Player.STATE_BUFFERING, Player.STATE_IDLE -> {}
                        }
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Toast.makeText(
                            requireContext(),
                            "Ошибка при воспроизведении: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }

        val playerAdapter = LeanbackPlayerAdapter(requireContext(), player!!, 500)

        playerGlue = VideoPlayerGlue(
            context = requireContext(),
            fragment = this,
            playerAdapter = playerAdapter
        ).apply {
            host = VideoSupportFragmentGlueHost(this@BasePlayerFragment)
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    protected fun preparePlayer(url: String) {
        player?.setMediaItem(MediaItem.fromUri(url.toUri()), false)
        player?.prepare()
    }

}
