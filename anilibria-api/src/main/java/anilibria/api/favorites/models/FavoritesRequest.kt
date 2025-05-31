package anilibria.api.favorites.models

import anilibria.api.collections.models.FavoritesFiltersRequest
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoritesRequest(
    @Json(name = "page")
    val page: Int?,
    @Json(name = "limit")
    val limit: Int?,
    @Json(name = "f")
    val filters: FavoritesFiltersRequest?
)