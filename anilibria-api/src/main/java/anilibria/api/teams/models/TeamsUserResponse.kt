package anilibria.api.teams.models


import anilibria.api.shared.UserResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamsUserResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "is_intern")
    val isIntern: Boolean,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "is_vacation")
    val isVacation: Boolean,
    @Json(name = "team")
    val team: TeamsTeamResponse,
    @Json(name = "user")
    val user: UserResponse,
    @Json(name = "roles")
    val roles: List<TeamsRoleResponse>
)