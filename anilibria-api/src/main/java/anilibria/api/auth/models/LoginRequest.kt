package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "login")
    val login: String,
    @Json(name = "password")
    val password: String
)