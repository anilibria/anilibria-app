package ru.radiationx.data.entity.response.release

import java.io.Serializable

data class BlockedInfoResponse(
    val isBlocked: Boolean,
    val reason: String?
) : Serializable