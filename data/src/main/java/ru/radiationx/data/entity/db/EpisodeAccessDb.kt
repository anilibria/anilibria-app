package ru.radiationx.data.entity.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpisodeAccessDb(
    @Json(name = "id") val id: String,
    @Json(name = "releaseId") val releaseId: Int,
    @Json(name = "seek") val seek: Long,
    @Json(name = "isViewed") val isViewed: Boolean,
    @Json(name = "lastAccess") val lastAccess: Long,
) 