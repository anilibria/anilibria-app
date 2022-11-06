package ru.radiationx.data.entity.domain.release

data class ReleaseUpdate(
    val id: Int,
    val timestamp: Int,
    val lastOpenTimestamp: Int
)