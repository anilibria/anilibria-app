package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseTypeResponse(
    // todo API2 can be null wtf
    @Json(name = "value")
    val value: String?,
    // todo API2 can be null wtf
    @Json(name = "description")
    val description: String?
)