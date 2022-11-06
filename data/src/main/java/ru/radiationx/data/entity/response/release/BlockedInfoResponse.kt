package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockedInfoResponse(
    @Json(name = "blocked") val isBlocked: Boolean,
    @Json(name = "reason") val reason: String?
)