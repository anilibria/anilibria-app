package anilibria.api.shared.filter


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilterSortingResponse(
    @Json(name = "value")
    val value: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "description")
    val description: String
)