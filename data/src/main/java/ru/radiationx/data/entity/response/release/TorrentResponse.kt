package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class TorrentResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "hash") val hash: String?,
    @Json(name = "leechers") val leechers: Int?,
    @Json(name = "seeders") val seeders: Int?,
    @Json(name = "completed") val completed: Int?,
    @Json(name = "quality") val quality: String?,
    @Json(name = "series") val series: String?,
    @Json(name = "size") val size: Long?,
    @Json(name = "url") val url: String?,
    @Json(name = "ctime") val date: Int?
) : Serializable 