package ru.radiationx.media.mobile.models

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

data class MediaItemTransition(
    val mediaItem: MediaItem?,
    val reason: Reason,
) {
    enum class Reason {
        REPEAT,
        AUTO,
        SEEK,
        PLAYLIST_CHANGED
    }
}

internal fun Int.asTransitionReason() = when (this) {
    Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> MediaItemTransition.Reason.REPEAT
    Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> MediaItemTransition.Reason.AUTO
    Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> MediaItemTransition.Reason.SEEK
    Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> MediaItemTransition.Reason.PLAYLIST_CHANGED
    else -> throw IllegalArgumentException("Unknown transition reason $this")
}
