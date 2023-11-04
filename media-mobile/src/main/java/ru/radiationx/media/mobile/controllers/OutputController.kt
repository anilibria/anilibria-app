package ru.radiationx.media.mobile.controllers

import android.graphics.Rect
import android.view.TextureView
import android.widget.FrameLayout
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.R
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.VideoOutputState
import ru.radiationx.media.mobile.views.AspectRatioFrameLayout

internal class OutputController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val mediaTextureView: TextureView,
    private val mediaAspectRatio: AspectRatioFrameLayout,
    private val scaleContainer: FrameLayout,
) : PlayerAttachListener {

    private val _state = MutableStateFlow(ScaleState())
    val state = _state.asStateFlow()

    private val _outputState = MutableStateFlow(VideoOutputState())
    val outputState = _outputState.asStateFlow()

    init {
        playerFlow.playerState
            .map { it.videoSize }
            .distinctUntilChanged()
            .onEach { videoSize ->
                _outputState.update { it.copy(videoSize = videoSize) }
                mediaAspectRatio.setAspectRatio(videoSize.aspectRatio)
            }
            .launchIn(coroutineScope)

        mediaAspectRatio.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val rect = Rect(0, 0, 0, 0)
            mediaAspectRatio.getGlobalVisibleRect(rect)
            _outputState.update { it.copy(hintRect = rect) }
        }
        scaleContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            _state.update { it.copy(fillScale = getFillScale()) }
        }

        _state.filter { !it.isLiveScale }.onEach { state ->
            val isFillScale = state.canApply && state.targetFill
            val scale = if (isFillScale) {
                getFillScale()
            } else {
                1f
            }
            val icRes = if (isFillScale) {
                R.drawable.ic_media_aspect_ratio_24
            } else {
                R.drawable.ic_media_settings_overscan_24
            }
            mediaAspectRatio.animate().scaleX(scale).scaleY(scale).start()
        }.launchIn(coroutineScope)
    }

    override fun attachPlayer(player: Player) {
        player.setVideoTextureView(mediaTextureView)
    }

    override fun detachPlayer(player: Player) {
        player.clearVideoTextureView(mediaTextureView)
    }

    fun setLiveScale(scale: Float?) {
        if (scale == null) {
            _state.update { it.copy(isLiveScale = false) }
            return
        }

        val internalState = _state.value
        val layoutScale = internalState.applyibleScale
        val coercedScale = scale.coerceIn(0.95f, layoutScale * 1.05f)
        val layoutScaleDiff = layoutScale - 1f

        _state.update {
            val targetFill = if (internalState.canApply) {
                coercedScale >= (1f + layoutScaleDiff / 2)
            } else {
                it.targetFill
            }
            it.copy(isLiveScale = true, targetFill = targetFill)
        }
        mediaAspectRatio.scaleX = coercedScale
        mediaAspectRatio.scaleY = coercedScale
    }

    fun toggleFill() {
        _state.update { it.copy(targetFill = !it.targetFill) }
    }

    fun updatePip(active: Boolean) {
        _state.update { it.copy(pip = active) }
    }

    private fun getFillScale(): Float {
        val videoWidth = mediaAspectRatio.width.toFloat()
        val videoHeight = mediaAspectRatio.height.toFloat()

        val scaleWidth = scaleContainer.width.toFloat()
        val scaleHeight = scaleContainer.height.toFloat()

        val widthPercent = scaleWidth / videoWidth
        val heightPercet = scaleHeight / videoHeight

        return maxOf(widthPercent, heightPercet).coerceAtLeast(1f)
    }

    data class ScaleState(
        val fillScale: Float = 1f,
        val pip: Boolean = false,
        val targetFill: Boolean = false,
        val isLiveScale: Boolean = false,
    ) {
        private val canFill = fillScale <= 1.5
        val applyibleScale = if (canFill) fillScale else 1f
        val canApply = canFill && !pip
    }
}