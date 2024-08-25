package anilibria.api.schedule.models


import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleResponse(
    @Json(name = "release")
    val release: ReleaseResponse,
    @Json(name = "new_release_episode")
    val newReleaseEpisode: ReleaseEpisodeResponse,
    @Json(name = "new_release_episode_ordinal")
    val newReleaseEpisodeOrdinal: Int
)