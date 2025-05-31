package anilibria.api.schedule.models


import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleResponse(
    @Json(name = "release")
    val release: ReleaseResponse,
    @Json(name = "full_season_is_released")
    val fullSeasonIsReleased: Boolean,
    @Json(name = "published_release_episode")
    val publishedReleaseEpisode: ReleaseEpisodeResponse?,
    @Json(name = "next_release_episode_number")
    val nextReleaseEpisodeNumber: Int?
)