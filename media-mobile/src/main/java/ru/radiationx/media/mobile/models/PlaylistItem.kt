package ru.radiationx.media.mobile.models

import androidx.media3.common.MediaItem

data class PlaylistItem(
    val mediaItem: MediaItem,
    val skips: List<TimelineSkip>,
)
