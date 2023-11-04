package ru.radiationx.media.mobile.models

import android.graphics.Rect

data class VideoOutputState(
    val hintRect: Rect = Rect(),
    val videoSize: VideoSizeState = VideoSizeState(),
)
