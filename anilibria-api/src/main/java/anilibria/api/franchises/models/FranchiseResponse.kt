package anilibria.api.franchises.models


import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FranchiseResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "name_english")
    val nameEnglish: String,
    @Json(name = "rating")
    val rating: Double,
    @Json(name = "last_year")
    val lastYear: Int,
    @Json(name = "first_year")
    val firstYear: Int,
    @Json(name = "total_releases")
    val totalReleases: Int,
    @Json(name = "total_episodes")
    val totalEpisodes: Int,
    @Json(name = "total_duration")
    val totalDuration: String,
    @Json(name = "total_duration_in_seconds")
    val totalDurationInSeconds: Int,
    @Json(name = "image")
    val image: ImageResponse,
    @Json(name = "franchise_releases")
    val franchiseReleases: List<FranchiseReleaseResponse>
)