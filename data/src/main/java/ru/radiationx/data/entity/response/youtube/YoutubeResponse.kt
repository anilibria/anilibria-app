package ru.radiationx.data.entity.response.youtube

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "vid") val vid: String?,
    @Json(name = "views") val views: Int,
    @Json(name = "comments") val comments: Int,
    @Json(name = "timestamp") val timestamp: Int
)