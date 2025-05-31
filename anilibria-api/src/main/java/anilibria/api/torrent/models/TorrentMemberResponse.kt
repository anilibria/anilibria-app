package anilibria.api.torrent.models

import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TorrentMemberResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "role")
    val role: Role,
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "external_url")
    val externalUrl: String?,
    @Json(name = "user")
    val user: User?
) {

    @JsonClass(generateAdapter = true)
    data class Role(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String?
    )

    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "id")
        val id: Int,
        @Json(name = "avatar")
        val avatar: ImageResponse?
    )
}
