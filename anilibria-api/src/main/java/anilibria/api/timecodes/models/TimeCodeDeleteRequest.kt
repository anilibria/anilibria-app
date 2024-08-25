package anilibria.api.timecodes.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeCodeDeleteRequest(
    @Json(name = "release_episode_id")
    val releaseEpisodeId: String
)