package ru.radiationx.data.entity.domain.auth

import ru.radiationx.data.apinext.models.OtpCode
import java.util.Date

data class OtpInfo(
    val code: OtpCode,
    val expiresAt: Date,
    val remainingTime: Long
)