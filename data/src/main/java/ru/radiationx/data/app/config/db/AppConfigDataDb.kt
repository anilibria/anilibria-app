package ru.radiationx.data.app.config.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppConfigDataDb(
    @Json(name = "addresses")
    val addresses: List<AppConfigAddressDb>
)
