package anilibria.api.shared

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "src")
    val rawSrc: String?,
    @Json(name = "preview")
    val rawPreview: String?,
    @Json(name = "thumbnail")
    val rawThumbnail: String?,
    @Json(name = "optimized")
    val optimized: Optimized
) {

    val src: String? = (rawSrc ?: rawPreview)?.let {
        "https://anilibria.top$it"
    }

    val thumbnail:String?= rawThumbnail?.let {
        "https://anilibria.top$it"
    }

    @JsonClass(generateAdapter = true)
    data class Optimized(
        @Json(name = "src")
        val rawSrc: String?,
        @Json(name = "preview")
        val rawPreview: String?,
        @Json(name = "thumbnail")
        val rawThumbnail: String?
    ) {

        val src: String? = (rawSrc ?: rawPreview)?.let {
            "https://anilibria.top$it"
        }

        val thumbnail:String?= rawThumbnail?.let {
            "https://anilibria.top$it"
        }
    }
}