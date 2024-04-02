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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.quill.get

@UnstableApi
open class BasePlayerFragment : VideoSupportFragment() {

    protected var playerGlue: VideoPlayerGlue? = null
        private set

    protected var player: ExoPlayer? = null
        private set

    protected var skipsPart: PlayerSkipsPart? = null
        private set

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initializePlayer()
        initializeRows()

        skipsPart = PlayerSkipsPart(
            parent = view as FrameLayout,
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