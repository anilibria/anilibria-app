package ru.radiationx.data.app.config.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppConfigAddressResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "widget") val widget: String,
    @Json(name = "site") val site: String,
    @Json(name = "image") val image: String,
    @Json(name = "api") val api: String,
    @Json(name = "status") val status: String,
)