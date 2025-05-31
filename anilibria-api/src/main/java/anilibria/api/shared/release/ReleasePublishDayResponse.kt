package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleasePublishDayResponse(
    @Json(name = "value")
    val value: Int,
    @Json(name = "description")
    val description: String
)