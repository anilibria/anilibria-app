package ru.radiationx.media.mobile.controllers.gesture

internal data class SeekerState(
    val isActive: Boolean = false,
    val initialSeek: Long = 0,
    val deltaSeek: Long = 0,
)