package ru.radiationx.data.app.menu.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkMenuResponse(
    @Json(name = "title") val title: String,
    @Json(name = "absoluteLink") val link: String?,
    @Json(name = "sitePagePath") val pagePath: String?,
    @Json(name = "icon") val icon: String?
)