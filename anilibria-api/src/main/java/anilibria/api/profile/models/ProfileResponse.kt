package anilibria.api.profile.models


import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "login")
    val login: String,
    @Json(name = "email")
    val email: String,
    @Json(name = "nickname")
    val nickname: String?,
    @Json(name = "avatar")
    val avatar: ImageResponse?,
    @Json(name = "torrents")
    val torrents: Torrents,
    @Json(name = "is_banned")
    val isBanned: Boolean,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "is_with_ads")
    val isWithAds: Boolean
) {

    @JsonClass(generateAdapter = true)
    data class Torrents(
        @Json(name = "passkey")
        val passkey: String,
        @Json(name = "uploaded")
        val uploaded: Long,
        @Json(name = "downloaded")
        val downloaded: Long
    )
}