package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseNameResponse(
    @Json(name = "main")
    val main: String,
    @Json(name = "english")
    val english: String,
    @Json(name = "alternative")
    val alternative: String?
)