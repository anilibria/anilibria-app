package anilibria.api.shared.release

import anilibria.api.shared.UserResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseMemberResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "user")
    val user: UserResponse?,
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
}