package ru.radiationx.data.app.menu.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkMenuDb(
    @Json(name = "title") val title: String,
    @Json(name = "absoluteLink") val link: String?,
    @Json(name = "sitePagePath") val pagePath: String?,
    @Json(name = "icon") val icon: String?
)