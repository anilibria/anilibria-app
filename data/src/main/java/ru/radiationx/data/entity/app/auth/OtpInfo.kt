package ru.radiationx.data.entity.app.auth

import java.util.*

data class OtpInfo(
    val code: String,
    val description: String,
    val expiresAt: Date
)