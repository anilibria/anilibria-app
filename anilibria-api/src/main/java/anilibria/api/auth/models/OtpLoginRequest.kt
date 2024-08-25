package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpLoginRequest(
    @Json(name = "code")
    val code: String,
    @Json(name = "device_id")
    val deviceId: String
)