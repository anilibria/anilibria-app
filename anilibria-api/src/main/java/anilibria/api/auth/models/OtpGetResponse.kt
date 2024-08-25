package anilibria.api.auth.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpGetResponse(
    @Json(name = "otp")
    val otp: Otp,
    @Json(name = "remaining_time")
    val remainingTime: Int
) {
    @JsonClass(generateAdapter = true)
    data class Otp(
        @Json(name = "code")
        val code: String,
        @Json(name = "users_id")
        val usersId: Int,
        @Json(name = "device_id")
        val deviceId: String,
        @Json(name = "expired_at")
        val expiredAt: String
    )
}