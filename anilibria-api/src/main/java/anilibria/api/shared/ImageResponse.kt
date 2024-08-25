package anilibria.api.shared

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "src")
    val src: String,
    @Json(name = "thumbnail")
    val thumbnail: String,
    @Json(name = "optmized")
    val optmized: Optmized
) {
    @JsonClass(generateAdapter = true)
    data class Optmized(
        @Json(name = "src")
        val src: String,
        @Json(name = "thumbnail")
        val thumbnail: String
    )
}