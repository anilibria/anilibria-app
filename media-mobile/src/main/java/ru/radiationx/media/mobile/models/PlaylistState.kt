package ru.radiationx.media.mobile.models

import androidx.media3.common.MediaItem

data class PlaylistState(
    val items: List<PlaylistItem> = emptyList(),
    val currentItem: PlaylistItem? = null,
) {

    fun findByMediaItem(mediaItem: MediaItem?): PlaylistItem? {
        return items.find { it.mediaItem == mediaItem }
    }
}