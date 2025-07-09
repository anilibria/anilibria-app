package anilibria.api.views.models

import anilibria.api.shared.release.ReleaseEpisodeResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ViewHistoryResponse(
    @Json(name = "id")
    val id: Long,
    @Json(name = "time")
    val time: Double,
    @Json(name = "user_id")
    val userId: Int,
    @Json(name = "is_watched")
    val isWatched: Boolean,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "release_episode_id")
    val releaseEpisodeId: String,
    @Json(name = "release_episode")
    val releaseEpisode: ReleaseEpisodeResponse?,
)