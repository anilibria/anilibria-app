package anilibria.api.shared.release


import anilibria.api.genres.models.GenreResponse
import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "type")
    val type: ReleaseTypeResponse,
    @Json(name = "year")
    val year: Int,
    @Json(name = "name")
    val name: ReleaseNameResponse,
    @Json(name = "alias")
    val alias: String,
    @Json(name = "season")
    val season: ReleaseSeasonResponse,
    @Json(name = "poster")
    val poster: ImageResponse,
    @Json(name = "fresh_at")
    val freshAt: String?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "is_ongoing")
    val isOngoing: Boolean,
    @Json(name = "age_rating")
    val ageRating: ReleaseAgeRatingResponse,
    @Json(name = "publish_day")
    val publishDay: ReleasePublishDayResponse,
    @Json(name = "description")
    val description: String?,
    @Json(name = "notification")
    val notification: String?,
    @Json(name = "episodes_total")
    val episodesTotal: Int?,
    @Json(name = "external_player")
    val externalPlayer: String?,
    @Json(name = "is_in_production")
    val isInProduction: Boolean,
    @Json(name = "is_blocked_by_geo")
    val isBlockedByGeo: Boolean,
    @Json(name = "is_blocked_by_copyrights")
    val isBlockedByCopyrights: Boolean,
    @Json(name = "added_in_users_favorites")
    val addedInUsersFavorites: Int,
    @Json(name = "average_duration_of_episode")
    val averageDurationOfEpisode: Int?,

    // semi-full
    @Json(name = "genres")
    val genres: List<GenreResponse>?,

    // full
    @Json(name = "members")
    val members: List<ReleaseMemberResponse>?,
    @Json(name = "episodes")
    val episodes: List<ReleaseEpisodeResponse>?,
    @Json(name = "torrents")
    val torrents: List<ReleaseTorrentResponse>?,
    @Json(name = "sponsor")
    val sponsor: ReleaseSponsorResponse?,

    // ignore
    @Json(name = "latest_episode")
    val latestEpisode: ReleaseEpisodeResponse?,
)