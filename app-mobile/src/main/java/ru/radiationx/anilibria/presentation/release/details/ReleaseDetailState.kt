package ru.radiationx.anilibria.presentation.release.details

import ru.radiationx.data.entity.app.release.ReleaseFull

data class ReleaseDetailState(
    val id:Int,
    val info: ReleaseInfoState,
    val favorite: ReleaseFavoriteState,
    val episodesControl: ReleaseEpisodesControlState,
    val episodes: List<ReleaseEpisodeItemState>,
    val torrents: List<ReleaseTorrentItemState>,
    val blockedInfo: ReleaseBlockedInfoState?
)

data class ReleaseInfoState(
    val titleRus: String,
    val titleEng: String,
    val description: String,
    val info: String,
    val days: List<Int>,
    val isOngoing: Boolean,
    val announce: String?
)

data class ReleaseFavoriteState(
    val rating: String,
    val isAdded: Boolean,
    val isRefreshing: Boolean
)

data class ReleaseEpisodeItemState(
    val id: Int,
    val title: String,
    val subtitle: String?,
    val isViewed: Boolean,
    val hasSd: Boolean,
    val hasHd: Boolean,
    val hasFullHd: Boolean,
    val type: ReleaseFull.Episode.Type
)

data class ReleaseTorrentItemState(
    val id: Int,
    val title: String,
    val subtitle: String,
    val size: String,
    val seeders: String,
    val leechers: String,
    val date: String?
)

data class ReleaseEpisodesControlState(
    val hasWeb: Boolean,
    val hasEpisodes: Boolean,
    val continueTitle: String
)

data class ReleaseBlockedInfoState(
    val title: String
)