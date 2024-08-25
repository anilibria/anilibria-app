package anilibria.api.timecodes.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeCodeNetwork(
    @Json(name = "release_episode_id")
    val releaseEpisodeId: String,
    @Json(name = "time")
    val time: Int,
    @Json(name = "is_watched")
    val isWatched: Boolean
)