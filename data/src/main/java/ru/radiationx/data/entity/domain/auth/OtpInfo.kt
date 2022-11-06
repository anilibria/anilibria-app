package ru.radiationx.data.entity.domain.auth

import java.util.*

data class OtpInfo(
    val code: String,
    val description: String,
    val expiresAt: Date,
    val remainingTime: Long
)