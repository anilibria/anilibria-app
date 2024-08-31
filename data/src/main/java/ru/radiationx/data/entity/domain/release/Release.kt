package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.apinext.models.ReleaseName
import ru.radiationx.data.apinext.models.ReleaseSponsor
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import java.util.Date

/* Created by radiationx on 31.10.17. */

@Parcelize
data class Release(
    // base
    val id: ReleaseId,
    val code: ReleaseCode,
    // todo API2 update usage
    val names: ReleaseName,
    // todo API2 update usage
    //val series: String?,
    val poster: String?,
    val createdAt: Date,
    val freshAt: Date,
    // todo API2 update usage (seconds to millis)
    val updatedAt: Date,
    // todo API2 update usage
    //val status: String?,
    // todo API2 update usage
    //val statusCode: String?,
    // todo API2 use this
    val isOngoing: Boolean,
    // todo API2 use this
    val isInProduction: Boolean,
    // todo API2 update usage
    val type: String,
    val genres: List<String>,
    // todo API2 update usage
    //val voices: List<String>,
    val year: Int,
    val season: String?,
    // todo API2 update usage
    val publishDay: String,
    val description: String?,
    val announce: String?,
    // todo API2 use this
    val favoritesCount: Int,
    // todo API2 update usage
    //val favoriteInfo: FavoriteInfo,
    // todo API2 update usage
    //val link: String?,
    // todo API2 update usage
    //val franchises: List<Franchise>,
    // todo API2 use this
    val ageRating: String,
    // todo API2 use this
    val episodesTotal: Int,
    // todo API2 use this
    val isEpisodesCountUnknown: Boolean,
    // todo API2 use this
    val averageEpisodeDuration: Int,
    // todo API2 use this
    val isBlockedByGeo: Boolean,
    // todo API2 use this
    val isBlockedByCopyrights: Boolean,
    val webPlayer: String?,


    // full
    val members: List<ReleaseMember>,
    val sponsor: ReleaseSponsor?,
    // todo API2 update usage
    //val showDonateDialog: Boolean,
    // todo API2 update usage
    //val blockedInfo: BlockedInfo,
    val episodes: List<Episode>,
    // todo API2 update usage
    //val sourceEpisodes: List<SourceEpisode>,
    val externalPlaylists: List<ExternalPlaylist>,
    val rutubePlaylist: List<RutubeEpisode>,
    val torrents: List<TorrentItem>,
) : Parcelable {


    companion object {
        const val STATUS_CODE_NOTHING = "0"
        const val STATUS_CODE_PROGRESS = "1"
        const val STATUS_CODE_COMPLETE = "2"
        const val STATUS_CODE_HIDDEN = "3"
        const val STATUS_CODE_NOT_ONGOING = "4"
    }

    val title: String?
        get() = names.firstOrNull()

    val titleEng: String?
        get() = names.lastOrNull()

    fun getFranchisesIds(): List<ReleaseId> {
        val ids = mutableListOf<ReleaseId>()
        franchises.forEach { franchise ->
            franchise.releases.forEach {
                ids.add(it.id)
            }
        }
        return ids
    }
}
