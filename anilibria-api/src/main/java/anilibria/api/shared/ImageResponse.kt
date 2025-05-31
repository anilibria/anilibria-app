package anilibria.api.shared

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "preview")
    val preview: String?,
    @Json(name = "thumbnail")
    val thumbnail: String?,
    @Json(name = "optimized")
    val optimized: Optimized?
) {

    @JsonClass(generateAdapter = true)
    data class Optimized(
        @Json(name = "preview")
        val preview: String?,
        @Json(name = "thumbnail")
        val thumbnail: String?
    )
}