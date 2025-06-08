package anilibria.api.timecodes.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeCodeNetwork(
    @Json(name = "release_episode_id")
    val releaseEpisodeId: String,
    @Json(name = "time")
    val time: Double,
    @Json(name = "is_watched")
    val isWatched: Boolean
) {

    companion object {
        fun ofList(list: List<Any>): TimeCodeNetwork {
            return TimeCodeNetwork(
                releaseEpisodeId = (list[0] as String),
                time = (list[1] as Number).toDouble(),
                isWatched = (list[2] as Boolean)
            )
        }
    }
}