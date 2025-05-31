package ru.radiationx.anilibria.ui.fragments.release.details

import com.yandex.mobile.ads.nativeads.NativeAd
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.data.apinext.models.ReleaseSponsor
import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.TorrentId
import java.util.Date

data class ReleaseDetailScreenState(
    val data: ReleaseDetailState? = null,
    val modifiers: ReleaseDetailModifiersState = ReleaseDetailModifiersState(),
    val remindText: String? = null,
    val donationCardState: DonationCardItemState? = null,
    val nativeAd: NativeAd? = null,
)

data class EpisodesTabState(
    val tag: String,
    val title: String,
    val textColor: Int?,
    val episodes: List<ReleaseEpisodeItemState>,
)

data class ReleaseDetailModifiersState(
    val selectedEpisodesTabTag: String? = null,
    val favoriteRefreshing: Boolean = false,
    val favoriteLoading: Boolean = false,
    val episodesReversed: Boolean = false,
    val descriptionExpanded: Boolean = false,
    val detailLoading: Boolean = true,
)

data class ReleaseDetailState(
    val id: ReleaseId,
    val info: ReleaseInfoState,
    val episodesControl: ReleaseEpisodesControlState?,
    val episodesTabs: List<EpisodesTabState>,
    val torrents: List<ReleaseTorrentItemState>,
    val blockedInfo: ReleaseBlockedInfoState?,
    val sponsor: ReleaseSponsor?
)

data class ReleaseInfoState(
    val titleRus: String,
    val titleEng: String,
    val freshAt: Date?,
    val description: String,
    val info: String,
    val publishDay: PublishDay,
    val needShowDay: Boolean,
    val announce: String?,
    val favorite: ReleaseFavoriteState,
) {
    companion object {
        const val TAG_GENRE = "genre"
        const val TAG_VOICE = "voice"
    }
}

data class ReleaseFavoriteState(
    val rating: String,
    val isAdded: Boolean,
)

data class ReleaseEpisodeItemState(
    val id: EpisodeId,
    val title: String,
    val subtitle: String?,
    val updatedAt: Date?,
    val isViewed: Boolean,
    val hasUpdate: Boolean,
    val hasSd: Boolean,
    val hasHd: Boolean,
    val hasFullHd: Boolean,
    val type: ReleaseEpisodeItemType,
    val tag: String,
    val actionTitle: String?,
    val actionColorRes: Int?,
    val actionIconRes: Int?,
    val hasActionUrl: Boolean,
)

enum class ReleaseEpisodeItemType {
    ONLINE, EXTERNAL, RUTUBE
}

data class ReleaseTorrentItemState(
    val id: TorrentId,
    val title: String,
    val subtitle: String,
    val size: String,
    val seeders: String,
    val leechers: String,
    val date: Date?,
    val isPrefer: Boolean,
    val progress: MutableStateFlow<Int>?,
)

data class ReleaseEpisodesControlState(
    val hasWeb: Boolean,
    val hasEpisodes: Boolean,
    val hasViewed: Boolean,
    val continueTitle: String,
)

data class ReleaseBlockedInfoState(
    val title: String,
)