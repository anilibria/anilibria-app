package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseTorrentResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "hash")
    val hash: String,
    @Json(name = "size")
    val size: Long,
    @Json(name = "type")
    val type: Type,
    @Json(name = "label")
    val label: String,
    @Json(name = "magnet")
    val magnet: String,
    @Json(name = "filename")
    val filename: String,
    @Json(name = "seeders")
    val seeders: Int,
    @Json(name = "quality")
    val quality: Quality,
    @Json(name = "codec")
    val codec: Codec,
    @Json(name = "color")
    val color: Color,
    @Json(name = "bitrate")
    val bitrate: Int,
    @Json(name = "leechers")
    val leechers: Int,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "completed_times")
    val completedTimes: Int
) {
    @JsonClass(generateAdapter = true)
    data class Type(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Quality(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Codec(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Color(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )
}