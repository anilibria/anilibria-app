package ru.radiationx.data.entity.app.release

import java.io.Serializable

data class BlockedInfo(
    val isBlocked: Boolean,
    val reason: String?
) : Serializable