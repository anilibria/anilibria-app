package ru.radiationx.data.app.historyfile.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpisodeAccessExport(
    @Json(name = "eid") val id: String,
    @Json(name = "rid") val releaseId: Int,
    @Json(name = "s") val seek: Long,
    @Json(name = "iv") val isViewed: Boolean,
    @Json(name = "la") val lastAccess: Long,
) 