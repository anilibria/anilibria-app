package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpGetRequest(
    @Json(name = "device_id")
    val deviceId: String
)