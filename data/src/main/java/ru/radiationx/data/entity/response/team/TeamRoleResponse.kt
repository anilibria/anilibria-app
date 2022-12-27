package ru.radiationx.data.entity.response.team


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamRoleResponse(
    @Json(name = "title")
    val title: String,
    @Json(name = "color")
    val color: String?
)