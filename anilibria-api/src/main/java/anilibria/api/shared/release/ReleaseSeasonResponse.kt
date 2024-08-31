package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseSeasonResponse(
    @Json(name = "value")
    val value: String?,
    @Json(name = "description")
    val description: String?
)