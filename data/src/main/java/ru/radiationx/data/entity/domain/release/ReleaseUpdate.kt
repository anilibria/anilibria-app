package ru.radiationx.data.entity.domain.release

import ru.radiationx.data.entity.domain.types.ReleaseId

data class ReleaseUpdate(
    val id: ReleaseId,
    val timestamp: Int,
    val lastOpenTimestamp: Int
)