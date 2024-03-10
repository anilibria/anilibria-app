package ru.radiationx.media.mobile.models

import androidx.media3.common.Player

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

internal fun Int.asPlaybackState(): PlaybackState = when (this) {
    Player.STATE_IDLE -> PlaybackState.IDLE
    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
    Player.STATE_READY -> PlaybackState.READY
    Player.STATE_ENDED -> PlaybackState.ENDED
    else -> throw IllegalArgumentException("Unknown playback state $this")
}