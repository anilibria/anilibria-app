package anilibria.api.torrent.models

import anilibria.api.shared.release.ReleaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TorrentResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "hash")
    val hash: String,
    @Json(name = "size")
    val size: Long,
    @Json(name = "type")
    val type: Type,
    @Json(name = "color")
    val color: Color,
    @Json(name = "codec")
    val codec: Codec,
    @Json(name = "label")
    val label: String,
    @Json(name = "quality")
    val quality: Quality,
    @Json(name = "magnet")
    val magnet: String,
    @Json(name = "filename")
    val filename: String,
    @Json(name = "seeders")
    val seeders: Int,
    @Json(name = "bitrate")
    val bitrate: Int?,
    @Json(name = "leechers")
    val leechers: Int,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "completed_times")
    val completedTimes: Int,
    @Json(name = "is_hardsub")
    val isHardsub: Boolean,
    @Json(name = "torrent_members")
    val torrentMembers: List<TorrentMemberResponse>?,
    @Json(name = "release")
    val release: ReleaseResponse?
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
        @Json(name = "label")
        val label: String,
        @Json(name = "description")
        val description: String,
        @Json(name = "label_color")
        val labelColor: String?,
        @Json(name = "label_is_visible")
        val labelIsVisible: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Color(
        @Json(name = "value")
        val value: String?,
        @Json(name = "description")
        val description: String?
    )
}