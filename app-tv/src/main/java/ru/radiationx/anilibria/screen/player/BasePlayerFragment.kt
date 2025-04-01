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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
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
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Разрешаем Leanback’у автоматически скрывать панель при воспроизведении
        isControlsOverlayAutoHideEnabled = true
        // 2) Разрешаем ручное сворачивание (и любые другие события пользователя)
        isShowOrHideControlsOverlayOnUserInteraction = true

        // Устанавливаем перехватчик клавиш. Он вызовется ПЕРЕД стандартной обработкой Leanback.
        // Если мы вернём true, событие не пойдёт дальше, и leanback-навигация по кнопкам не сработает.
        // Поэтому "глотаем" только Play/Pause, а всё остальное возвращаем false.
        setOnKeyInterceptListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                Log.d("BasePlayerFragment", "KEYCODE_MEDIA_PLAY_PAUSE pressed")

                // Переключаем плеер вручную:
                if (playerGlue?.isPlaying == true) {
                    playerGlue?.pause()
                } else {
                    playerGlue?.play()
                }

                // Показываем оверлей (с его автоскрытием).
                showControlsOverlay(false)

                // Возвращаем true → событие "съедено" этим перехватчиком.
                true
            } else {
                // Для остальных кнопок даём Leanback делать своё дело
                false
            }
        }

        // Оставшаяся инициализация
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initializePlayer()
        initializeRows()

        // Подключаем skip-логику
        skipsPart = PlayerSkipsPart(
            parent = view as FrameLayout,
            onSeek = { position -> player?.seekTo(position) },
            onSkipShow = {
                // Пока skip показан, запрещаем автоскрытие
                isShowOrHideControlsOverlayOnUserInteraction = false
                hideControlsOverlay(false)
            },
            onSkipHide = {
                // Когда skip убрали, снова включаем автоскрытие
                isShowOrHideControlsOverlayOnUserInteraction = true
            }
        )

        // По ходу воспроизведения обновляем skip
        playerGlue?.playbackListener = object : VideoPlayerGlue.PlaybackListener {
            override fun onUpdateProgress() {
                skipsPart?.update(player?.currentPosition ?: 0)
            }
        }

        // "Хак" для случаев, когда при нажатии "ОК" оверлей не прятался
        fadeCompleteListener = object : OnFadeCompleteListener() {
            override fun onFadeInComplete() {
                super.onFadeInComplete()
                // Перезапускаем флаг автоскрытия (иногда помогает, если есть глюки)
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

    @OptIn(UnstableApi::class)
    override fun onDestroyView() {
        super.onDestroyView()
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
                        // Здесь вы ловите любую ошибку плеера, в т.ч. сеть/IO
                        // Можно проверить error.errorCode или error.cause
                        Toast.makeText(
                            requireContext(),
                            "Ошибка при воспроизведении: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }

        val playerAdapter = LeanbackPlayerAdapter(requireContext(), player!!, 500)

        // Передаём ссылку на свой fragment в VideoPlayerGlue
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

    /**
     * Вызывайте это, чтобы подготовить плеер к воспроизведению URL. Например:
     * preparePlayer("https://site.com/video.mp4")
     */
    protected fun preparePlayer(url: String) {
        player?.setMediaItem(MediaItem.fromUri(url.toUri()), false)
        player?.prepare()
    }
}
