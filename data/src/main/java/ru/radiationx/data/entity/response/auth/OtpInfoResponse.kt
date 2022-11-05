package ru.radiationx.data.entity.response.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class OtpInfoResponse(
    @Json(name = "code") val code: String,
    @Json(name = "description") val description: String,
    @Json(name = "expiredAt") val expiredAt: Long,
    @Json(name = "remainingTime") val remainingTime: Long
)