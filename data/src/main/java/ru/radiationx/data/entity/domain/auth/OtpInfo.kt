package ru.radiationx.data.entity.domain.auth

import java.util.Date

data class OtpInfo(
    val code: String,
    val expiresAt: Date,
    val remainingTime: Long
)