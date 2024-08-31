package anilibria.api.videos.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoOriginResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "origin")
    val origin: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "is_announce")
    val isAnnounce: Boolean
)