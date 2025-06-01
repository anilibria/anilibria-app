package ru.radiationx.data.app.historyfile.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseUpdateExport(
    @Json(name = "rid") val id: Int,
    @Json(name = "ts") val timestamp: Int,
    @Json(name = "lots") val lastOpenTimestamp: Int
)