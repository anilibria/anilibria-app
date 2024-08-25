package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionTypeResponse(
    @Json(name = "value")
    val value: String,
    @Json(name = "description")
    val description: String
)