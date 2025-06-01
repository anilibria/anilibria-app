package ru.radiationx.data.app.historyfile.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseHistoryExport(
    @Json(name = "rid") val id: Int,
)