package anilibria.api.shared

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id")
    val id: Int,
    // todo API2 nickname can be null wtf how
    @Json(name = "nickname")
    val nickname: String?,
    @Json(name = "avatar")
    val avatar: ImageResponse?
)