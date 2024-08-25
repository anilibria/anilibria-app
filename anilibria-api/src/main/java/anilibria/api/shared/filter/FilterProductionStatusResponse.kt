package anilibria.api.shared.filter


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilterProductionStatusResponse(
    @Json(name = "value")
    val value: String,
    @Json(name = "description")
    val description: String
)