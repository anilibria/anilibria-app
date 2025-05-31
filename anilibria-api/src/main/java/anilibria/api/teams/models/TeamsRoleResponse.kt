package anilibria.api.teams.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamsRoleResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "color")
    val color: String?,
    @Json(name = "sort_order")
    val sortOrder: Int
)