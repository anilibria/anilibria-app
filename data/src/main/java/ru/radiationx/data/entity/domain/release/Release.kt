package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.apinext.models.Genre
import ru.radiationx.data.apinext.models.ReleaseCounters
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.apinext.models.ReleaseName
import ru.radiationx.data.apinext.models.ReleaseSponsor
import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import java.util.Date

/* Created by radiationx on 31.10.17. */

@Parcelize
data class Release(
    // base
    val id: ReleaseId,
    val code: ReleaseCode,
    val names: ReleaseName,
    val poster: Url.Relative?,
    val createdAt: Date,
    val freshAt: Date,
    val updatedAt: Date,
    val isOngoing: Boolean,
    val isInProduction: Boolean,
    val type: String?,
    val year: Int,
    val season: String?,
    val publishDay: PublishDay,
    val description: String?,
    val announce: String?,
    val counters: ReleaseCounters,
    val ageRating: String,
    val episodesTotal: Int?,
    val averageEpisodeDuration: Int?,
    val isBlockedByGeo: Boolean,
    val isBlockedByCopyrights: Boolean,
    val webPlayer: String?,

    // semi full
    val genres: List<Genre>,

    // full
    val members: List<ReleaseMember>,
    val sponsor: ReleaseSponsor?,
    val episodes: List<Episode>,
    val externalPlaylists: List<ExternalPlaylist>,
    val rutubePlaylist: List<RutubeEpisode>,
    val torrents: List<TorrentItem>,
) : Parcelable {

    val blockingReason: String?
        get() = when {
            isBlockedByGeo -> "Контент заблокирован на территории вашей страны"
            isBlockedByCopyrights -> "Контент заблокирован по запросу правообладателей"
            else -> null
        }

    val link: Url.Relative
        get() = Url.relativeOf("/anime/releases/release/${code.code}")
}
