package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginSocialResponse(
    @Json(name = "url")
    val url: String,
    @Json(name = "state")
    val state: String
)