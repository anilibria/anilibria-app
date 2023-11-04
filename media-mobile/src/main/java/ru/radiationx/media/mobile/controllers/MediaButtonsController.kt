package ru.radiationx.media.mobile.controllers

import android.view.View
import android.widget.ImageButton
import androidx.core.view.isInvisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.R
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.PlayButtonState
import ru.radiationx.media.mobile.models.PlaybackState
import ru.radiationx.media.mobile.models.PlayerState
import ru.radiationx.shared.ktx.android.setCompatDrawable

internal class MediaButtonsController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val mediaButtonPrev: ImageButton,
    private val mediaButtonPlay: ImageButton,
    private val mediaButtonNext: ImageButton,
) : PlayerAttachListener {

    var onAnyTap: (() -> Unit)? = null

    private val _playButtonState = MutableStateFlow(PlayButtonState.PLAY)
    val playButtonState = _playButtonState.asStateFlow()

    init {
        mediaButtonPrev.setOnClickListener {
            onAnyTap?.invoke()
            playerFlow.prev()
        }
        mediaButtonPlay.setOnClickListener {
            onAnyTap?.invoke()
            handlePlayClick()
        }
        mediaButtonNext.setOnClickListener {
            onAnyTap?.invoke()
            playerFlow.next()
        }

        playerFlow.playerState
            .map { it.toPlayButtonState() }
            .distinctUntilChanged()
            .map { _playButtonState.value = it }
            .launchIn(coroutineScope)

        playerFlow.playerState
            .map { it.isBlockingLoading || !it.commands.playPause }
            .distinctUntilChanged()
            .map { mediaButtonPlay.isInvisible = it }
            .launchIn(coroutineScope)

        _playButtonState.onEach {
            val icRes = when (it) {
                PlayButtonState.PLAY -> R.drawable.ic_media_play_arrow_24
                PlayButtonState.PAUSE -> R.drawable.ic_media_pause_24
                PlayButtonState.REPLAY -> R.drawable.ic_media_replay_24
            }
            mediaButtonPlay.setCompatDrawable(icRes)
        }.launchIn(coroutineScope)

        playerFlow.playerState.map { it.commands }.distinctUntilChanged().onEach {
            mediaButtonPrev.applyEnabled(it.seekToPreviousMediaItem)
            mediaButtonNext.applyEnabled(it.seekToNextMediaItem)
        }.launchIn(coroutineScope)
    }

    fun handlePlayClick() {
        when (playButtonState.value) {
            PlayButtonState.PLAY -> playerFlow.play()
            PlayButtonState.PAUSE -> playerFlow.pause()
            PlayButtonState.REPLAY -> {
                playerFlow.seekTo(0)
                playerFlow.play()
            }
        }
    }

    private fun View.applyEnabled(state: Boolean) {
        isEnabled = state
        val alpha = if (state) 1.0f else 0.5f
        animate().alpha(alpha).start()
    }

    private fun PlayerState.toPlayButtonState(): PlayButtonState = when {
        !isPlaying && playbackState == PlaybackState.ENDED -> PlayButtonState.REPLAY
        playWhenReady -> PlayButtonState.PAUSE
        else -> PlayButtonState.PLAY
    }
}