package anilibria.api.collections.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionsRequest(
    @Json(name = "type_of_collection")
    val typeOfCollection: String,
    @Json(name = "page")
    val page: Int?,
    @Json(name = "limit")
    val limit: Int?,
    @Json(name = "f")
    val filters: CollectionsFiltersRequest?
)