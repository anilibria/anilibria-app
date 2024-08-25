package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpAcceptRequest(
    @Json(name = "code")
    val code: String
)