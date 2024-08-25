package ru.radiationx.data.entity.domain.release

import ru.radiationx.data.entity.domain.types.ReleaseId
import java.util.Date

data class ReleaseUpdate(
    val id: ReleaseId,
    val timestamp: Date,
    val lastOpenTimestamp: Date
)