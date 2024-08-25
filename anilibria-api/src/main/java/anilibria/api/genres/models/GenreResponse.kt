package anilibria.api.genres.models


import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenreResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "total_releases")
    val totalReleases: Int,
    @Json(name = "image")
    val image: ImageResponse
)