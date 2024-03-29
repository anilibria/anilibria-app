package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExternalPlaylistResponse(
    @Json(name = "tag") val tag: String,
    @Json(name = "title") val title: String,
    @Json(name = "actionText") val actionText: String,
    @Json(name = "episodes") val episodes: List<ExternalEpisodeResponse>
)