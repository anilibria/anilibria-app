package ru.radiationx.anilibria.presentation.release.details

import ru.radiationx.data.entity.app.release.ReleaseFull

data class ReleaseDetailScreenState(
    val data: ReleaseDetailState? = null,
    val episodesType: ReleaseFull.Episode.Type = ReleaseFull.Episode.Type.ONLINE,
    val remindText: String? = null
)

data class ReleaseDetailState(
    val id: Int,
    val info: ReleaseInfoState,
    val episodesControl: ReleaseEpisodesControlState,
    val episodes: Map<ReleaseFull.Episode.Type, List<ReleaseEpisodeItemState>>,
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
    val announce: String?,
    val favorite: ReleaseFavoriteState
)

data class ReleaseFavoriteState(
    val rating: String,
    val isAdded: Boolean,
    val isRefreshing: Boolean
)

data class ReleaseEpisodeItemState(
    val id: Int,
    val releaseId: Int,
    val title: String,
    val subtitle: String?,
    val isViewed: Boolean,
    val hasSd: Boolean,
    val hasHd: Boolean,
    val hasFullHd: Boolean
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
    val hasViewed: Boolean,
    val continueTitle: String
)

data class ReleaseBlockedInfoState(
    val title: String
)