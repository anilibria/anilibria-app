package anilibria.api.shared.release

import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseEpisodeResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "ordinal")
    val ordinal: Double,
    @Json(name = "opening")
    val opening: Skip,
    @Json(name = "ending")
    val ending: Skip,
    @Json(name = "preview")
    val preview: ImageResponse,
    @Json(name = "hls_480")
    val hls480: String,
    @Json(name = "hls_720")
    val hls720: String,
    @Json(name = "hls_1080")
    val hls1080: String,
    @Json(name = "duration")
    val duration: Int,
    @Json(name = "rutube_id")
    val rutubeId: String,
    @Json(name = "youtube_id")
    val youtubeId: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "name_english")
    val nameEnglish: String,
    @Json(name = "release")
    val release: ReleaseResponse?,
) {
    @JsonClass(generateAdapter = true)
    data class Skip(
        @Json(name = "start")
        val start: Int,
        @Json(name = "stop")
        val stop: Int
    )
}