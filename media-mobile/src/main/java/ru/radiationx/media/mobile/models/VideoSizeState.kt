package ru.radiationx.media.mobile.models

import androidx.media3.common.VideoSize

data class VideoSizeState(
    val width: Int = 0,
    val height: Int = 0,
    val aspectRatio: Float = DEFAULT_ASPECT_RATIO,
) {
    companion object {
        const val DEFAULT_ASPECT_RATIO = 16f / 9f
    }
}

internal fun VideoSize.toState(): VideoSizeState {
    val videoAspectRatio = if (height == 0 || width == 0) {
        VideoSizeState.DEFAULT_ASPECT_RATIO
    } else {
        width * pixelWidthHeightRatio / height
    }
    return VideoSizeState((width * pixelWidthHeightRatio).toInt(), height, videoAspectRatio)
}
