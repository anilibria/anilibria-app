package anilibria.api.shared.filter


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilterGenreResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)