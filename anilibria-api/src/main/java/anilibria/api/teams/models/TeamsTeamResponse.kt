package anilibria.api.teams.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeamsTeamResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "description")
    val description: String?
)