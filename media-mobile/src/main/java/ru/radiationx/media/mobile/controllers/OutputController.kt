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

    companion object {
        private val fitScale = Scale(1f, 1f)
    }

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
            val scale = state.getScaleFor(state.target)
            mediaAspectRatio.animate().scaleX(scale.x).scaleY(scale.y).start()
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
        val nextTarget = when (internalState.target) {
            ScaleType.Fit -> ScaleType.Crop
            ScaleType.Crop -> ScaleType.Crop
            ScaleType.Fill -> ScaleType.Fill
        }
        val layoutScale = internalState.getScaleFor(nextTarget)

        val coercedScale = Scale(
            scale.coerceIn(0.95f, layoutScale.x * 1.05f),
            scale.coerceIn(0.95f, layoutScale.y * 1.05f)
        )
        val layoutScaleDiff = Scale(
            layoutScale.x - 1f,
            layoutScale.y - 1f
        )
        val newTarget = if (internalState.canApply) {
            if (coercedScale.x >= (1f + layoutScaleDiff.x / 2) || coercedScale.y >= (1f + layoutScaleDiff.y / 2)) {
                nextTarget
            } else {
                ScaleType.Fit
            }
        } else {
            internalState.target
        }
        _state.update {
            it.copy(isLiveScale = true, target = newTarget)
        }
        mediaAspectRatio.scaleX = coercedScale.x
        mediaAspectRatio.scaleY = coercedScale.y
    }

    fun toggleTarget() {
        _state.update {
            it.copy(target = it.nextTarget())
        }
    }

    fun updatePip(active: Boolean) {
        _state.update { it.copy(pip = active) }
    }

    private fun getFillScale(): Scale {
        val videoWidth = mediaAspectRatio.width.toFloat()
        val videoHeight = mediaAspectRatio.height.toFloat()

        val scaleWidth = scaleContainer.width.toFloat()
        val scaleHeight = scaleContainer.height.toFloat()

        val widthPercent = scaleWidth / videoWidth
        val heightPercet = scaleHeight / videoHeight

        return Scale(widthPercent, heightPercet)
    }

    data class ScaleState(
        val fillScale: Scale = fitScale,
        val pip: Boolean = false,
        val target: ScaleType = ScaleType.Fit,
        val isLiveScale: Boolean = false,
    ) {
        private val canFill = fillScale.maxCorced <= 1.5
        val canApply = canFill && !pip

        fun getScaleFor(type: ScaleType): Scale {
            if (!canApply) {
                return fitScale
            }
            return when (type) {
                ScaleType.Fit -> fitScale
                ScaleType.Crop -> Scale(fillScale.maxCorced, fillScale.maxCorced)
                ScaleType.Fill -> fillScale
            }
        }

        fun nextTarget(): ScaleType {
            return when (target) {
                ScaleType.Fit -> ScaleType.Crop
                ScaleType.Crop -> ScaleType.Fill
                ScaleType.Fill -> ScaleType.Fit
            }
        }
    }

    data class Scale(
        val x: Float,
        val y: Float,
    ) {
        val maxCorced: Float = maxOf(x, y).coerceAtLeast(1f)
    }

    enum class ScaleType {
        Fit,
        Crop,
        Fill
    }
}