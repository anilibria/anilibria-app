package ru.radiationx.data.entity.app.team


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamUserResponse(
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "roles")
    val roles: List<TeamRoleResponse>,
    @Json(name = "is_intern")
    val isIntern: Boolean,
    @Json(name = "is_vacation")
    val isVacation: Boolean
)