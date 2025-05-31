package anilibria.api.catalog.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogFiltersRequest(
    @Json(name = "genres")
    val genres: List<String>?,
    @Json(name = "types")
    val types: List<String>?,
    @Json(name = "seasons")
    val seasons: List<String>?,
    @Json(name = "years")
    val years: Years?,
    @Json(name = "search")
    val search: String?,
    @Json(name = "sorting")
    val sorting: String?,
    @Json(name = "age_ratings")
    val ageRatings: List<String>?,
    @Json(name = "publish_statuses")
    val publishStatuses: List<String>?,
    @Json(name = "production_statuses")
    val productionStatuses: List<String>?
) {

    @JsonClass(generateAdapter = true)
    data class Years(
        @Json(name = "from_year")
        val from: Int,
        @Json(name = "to_year")
        val to: Int
    )
}