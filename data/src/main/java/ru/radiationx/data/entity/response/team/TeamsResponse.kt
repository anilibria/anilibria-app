package ru.radiationx.data.entity.response.team


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamsResponse(
    @Json(name = "header_roles")
    val headerRoles: List<TeamRoleResponse>,
    @Json(name = "teams")
    val teams: List<TeamResponse>
)