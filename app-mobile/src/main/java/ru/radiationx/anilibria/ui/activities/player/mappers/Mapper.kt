package ru.radiationx.anilibria.ui.activities.player.mappers

import android.net.Uri
import androidx.media3.common.MediaItem
import ru.radiationx.anilibria.ui.activities.player.models.EpisodeState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerData
import ru.radiationx.anilibria.ui.activities.player.models.PlayerDataState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerRelease
import ru.radiationx.data.api.releases.models.Episode
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.media.mobile.models.PlaylistItem
import ru.radiationx.media.mobile.models.TimelineSkip


fun Release.toPlayerRelease() = PlayerRelease(
    id = id,
    name = names.main,
    episodes = episodes
)

fun PlayerData.toDataState(episodeId: EpisodeId): PlayerDataState? {
    return getRelease(episodeId.releaseId)?.toDataState(episodeId)
}

fun PlayerRelease.toDataState(episodeId: EpisodeId) = PlayerDataState(
    id = id,
    title = name,
    episodeTitle = episodes.find { it.id == episodeId }?.title.orEmpty()
)

fun Episode.toState(quality: PlayerQuality) = EpisodeState(
    id = id,
    title = title.orEmpty(),
    url = qualityInfo.getSafeUrlFor(quality),
    skips = skips
)

fun EpisodeState.toPlaylistItem(): PlaylistItem {
    val mediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(url))
        .setTag(this)
        .build()
    val skips = listOfNotNull(skips?.opening, skips?.ending).map {
        TimelineSkip(it.start, it.end)
    }
    return PlaylistItem(mediaItem, skips)
}