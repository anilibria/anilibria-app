package anilibria.api.shared.release

import anilibria.api.shared.ImageResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseMemberResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "user")
    val user: User?,
    @Json(name = "role")
    val role: Role,
    @Json(name = "nickname")
    val nickname: String
) {

    @JsonClass(generateAdapter = true)
    data class Role(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "id")
        val id: Int,
        @Json(name = "avatar")
        val avatar: ImageResponse?
    )
}