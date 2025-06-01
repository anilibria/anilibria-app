package ru.radiationx.data.app.releaseupdate.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseUpdateDb(
    @Json(name = "id") val id: Int,
    @Json(name = "timestamp") val timestamp: Int,
    @Json(name = "lastOpenTimestamp") val lastOpenTimestamp: Int
)