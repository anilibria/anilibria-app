package ru.radiationx.data.app.episodeaccess.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpisodeAccessLegacyDb(
    @Json(name = "id") val id: Int,
    @Json(name = "releaseId") val releaseId: Int,
    @Json(name = "seek") val seek: Long,
    @Json(name = "isViewed") val isViewed: Boolean,
    @Json(name = "lastAccess") val lastAccess: Long,
) 