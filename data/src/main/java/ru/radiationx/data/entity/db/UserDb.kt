package ru.radiationx.data.entity.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDb(
    @Json(name = "id")
    val id: Int,
    @Json(name = "nickname")
    val nickname: String?,
    @Json(name = "avatar")
    val avatar: String?
)