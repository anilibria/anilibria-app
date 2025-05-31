package anilibria.api.catalog.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogRequest(
    @Json(name = "page")
    val page: Int?,
    @Json(name = "limit")
    val limit: Int?,
    @Json(name = "f")
    val filters: CatalogFiltersRequest?
)