package ru.radiationx.data.app.history.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseHistoryDb(
    @Json(name = "id")
    val id: Int
)