package ru.radiationx.data.entity.response.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SocialAuthResponse(
    @Json(name = "key") val key: String,
    @Json(name = "title") val title: String,
    @Json(name = "socialUrl") val socialUrl: String,
    @Json(name = "resultPattern") val resultPattern: String,
    @Json(name = "errorUrlPattern") val errorUrlPattern: String
)