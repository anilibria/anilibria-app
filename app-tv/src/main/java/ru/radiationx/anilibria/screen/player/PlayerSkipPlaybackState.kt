package ru.radiationx.anilibria.screen.player

internal class PlayerSkipPlaybackState {
    var hasFrame = false
        private set
    var isReady = false
        private set
    var isPlaying = false
        private set

    val canShow: Boolean get() = hasFrame && isReady

    fun framePending(positionChanged: Boolean = true) {
        // Media3 may report a seek to the same position without rendering another first frame.
        if (positionChanged) hasFrame = false
    }

    fun frameRendered() {
        hasFrame = true
    }

    fun update(isReady: Boolean, isPlaying: Boolean) {
        this.isReady = isReady
        this.isPlaying = isPlaying
    }

    fun canCount(appearanceFinished: Boolean, watchFocused: Boolean): Boolean =
        canShow && isPlaying && appearanceFinished && !watchFocused
}
