package anilibria.api.collections.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoritesFiltersRequest(
    @Json(name = "genres")
    val genres: String?,
    @Json(name = "types")
    val types: List<String>?,
    @Json(name = "years")
    val years: String?,
    @Json(name = "search")
    val search: String?,
    @Json(name = "sorting")
    val sorting: String?,
    @Json(name = "age_ratings")
    val ageRatings: List<String>?
)