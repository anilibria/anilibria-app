package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseAgeRatingResponse(
    @Json(name = "value")
    val value: String,
    @Json(name = "label")
    val label: String,
    @Json(name = "is_adult")
    val isAdult: Boolean,
    @Json(name = "description")
    val description: String
)