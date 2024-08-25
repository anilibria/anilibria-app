package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeRatingResponse(
    @Json(name = "value")
    val value: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "description")
    val description: String
)