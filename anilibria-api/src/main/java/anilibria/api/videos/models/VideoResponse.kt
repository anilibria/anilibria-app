package anilibria.api.videos.models


import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "url")
    val url: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "views")
    val views: Int,
    @Json(name = "comments")
    val comments: Int,
    @Json(name = "video_id")
    val videoId: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "is_announce")
    val isAnnounce: Boolean,
    @Json(name = "image")
    val image: ImageResponse,
    @Json(name = "origin")
    val origin: VideoOriginResponse
)