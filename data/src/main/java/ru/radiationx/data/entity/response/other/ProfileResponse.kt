package ru.radiationx.data.entity.response.other

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "login") val nick: String?,
    @Json(name = "avatar") val avatarUrl: String?,
)
