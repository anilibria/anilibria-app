package ru.radiationx.data.entity.response.auth

import java.util.*

data class OtpInfoResponse(
    val code: String,
    val description: String,
    val expiresAt: Date,
    val remainingTime: Long
)