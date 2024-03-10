package ru.radiationx.anilibria.ui.activities.player.ext

import androidx.media3.common.MediaItem
import ru.radiationx.anilibria.ui.activities.player.models.EpisodeState
import ru.radiationx.media.mobile.models.PlaylistItem

fun PlaylistItem.getEpisode(): EpisodeState {
    return mediaItem.getEpisode()
}

fun MediaItem.getEpisode(): EpisodeState {
    return localConfiguration?.tag as EpisodeState
}