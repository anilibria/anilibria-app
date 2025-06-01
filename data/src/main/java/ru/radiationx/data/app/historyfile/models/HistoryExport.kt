package ru.radiationx.data.app.historyfile.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryExport(
    @Json(name = "history") val history: List<ReleaseHistoryExport>,
    @Json(name = "updates") val updates: List<ReleaseUpdateExport>,
    @Json(name = "episodes") val episodes: List<EpisodeAccessExport>,
)