package ru.radiationx.media.mobile.models

data class PlayerState(
    val playWhenReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val videoSize: VideoSizeState = VideoSizeState(),
    val error: PlayerErrorState? = null,
    val commands: PlayerCommandsState = PlayerCommandsState(),
) {
    val isBlockingLoading = !isPlaying && isLoading && playbackState == PlaybackState.BUFFERING
}