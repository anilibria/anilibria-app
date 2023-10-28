package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpisodeResponse(
    @Json(name = "id") val id: Float,
    @Json(name = "title") val title: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "sd") val urlSd: String?,
    @Json(name = "hd") val urlHd: String?,
    @Json(name = "fullhd") val urlFullHd: String?,
    @Json(name = "srcSd") val srcUrlSd: String?,
    @Json(name = "srcHd") val srcUrlHd: String?,
    @Json(name = "srcFullHd") val srcUrlFullHd: String?,
    @Json(name = "updated_at") val updatedAt: Int?,
    @Json(name = "skips") val skips: PlayerSkipsResponse?,
    @Json(name = "sources") val sources: SourceTypesResponse?,
    @Json(name = "rutube_id") val rutubeId: String?,
)