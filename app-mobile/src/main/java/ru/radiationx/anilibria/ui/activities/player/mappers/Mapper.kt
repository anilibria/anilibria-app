package ru.radiationx.anilibria.ui.activities.player.mappers

import android.net.Uri
import androidx.media3.common.MediaItem
import ru.radiationx.anilibria.ui.activities.player.models.EpisodeState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerDataState
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.media.mobile.models.PlaylistItem
import ru.radiationx.media.mobile.models.TimelineSkip


fun Release.toDataState(episodeId: EpisodeId) = PlayerDataState(
    id = id,
    title = (title ?: titleEng).orEmpty(),
    episodeTitle = episodes.find { it.id == episodeId }?.title.orEmpty()
)

fun Episode.toState(quality: PlayerQuality) = EpisodeState(
    id = id,
    title = title.orEmpty(),
    url = qualityInfo.getUrlFor(quality),
    skips = skips
)

fun EpisodeState.toPlaylistItem(): PlaylistItem {
    val uri = url?.let { Uri.parse(it) }
    val mediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setTag(this)
        .build()
    val skips = listOfNotNull(skips?.opening, skips?.ending).map {
        TimelineSkip(it.start, it.end)
    }
    return PlaylistItem(mediaItem, skips)
}