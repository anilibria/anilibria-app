package ru.radiationx.media.mobile.controllers

import android.widget.ImageButton
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.R
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.shared.ktx.android.setCompatDrawable

internal class MediaActionsController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val mediaActionPip: ImageButton,
    private val mediaActionScale: ImageButton,
    private val mediaActionSettings: ImageButton,
    private val mediaActionFullscreen: ImageButton,
) : PlayerAttachListener {

    var onAnyTap: (() -> Unit)? = null

    var onPipClick: (() -> Unit)? = null
    var onScaleClick: (() -> Unit)? = null
    var onSettingsClick: (() -> Unit)? = null
    var onFullscreenClick: (() -> Unit)? = null

    init {
        mediaActionPip.setOnClickListener {
            onAnyTap?.invoke()
            onPipClick?.invoke()
        }

        mediaActionScale.setOnClickListener {
            onAnyTap?.invoke()
            onScaleClick?.invoke()
        }

        mediaActionSettings.setOnClickListener {
            onAnyTap?.invoke()
            onSettingsClick?.invoke()
        }

        mediaActionFullscreen.setOnClickListener {
            onAnyTap?.invoke()
            onFullscreenClick?.invoke()
        }
    }


    fun setPipVisible(state: Boolean) {
        mediaActionPip.isVisible = state
    }

    fun setFullscreenVisible(state: Boolean) {
        mediaActionFullscreen.isVisible = state
    }

    fun setFullscreenActive(state: Boolean) {
        val icRes = if (state) {
            R.drawable.ic_media_fullscreen_exit_24
        } else {
            R.drawable.ic_media_fullscreen_24
        }
        mediaActionFullscreen.setCompatDrawable(icRes)
    }

    fun setScaleVisible(state: Boolean) {
        mediaActionScale.isVisible = state
    }

    fun setScaleFill(state: Boolean) {
        val icRes = if (state) {
            R.drawable.ic_media_aspect_ratio_24
        } else {
            R.drawable.ic_media_settings_overscan_24
        }
        mediaActionScale.setCompatDrawable(icRes)
    }

}