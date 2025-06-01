package ru.radiationx.data.api.auth.models

import java.util.Date

data class OtpInfo(
    val code: OtpCode,
    val expiresAt: Date,
    val remainingTime: Long
)