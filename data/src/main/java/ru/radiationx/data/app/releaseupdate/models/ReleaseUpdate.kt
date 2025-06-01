package ru.radiationx.data.app.releaseupdate.models

import ru.radiationx.data.common.ReleaseId
import java.util.Date

data class ReleaseUpdate(
    val id: ReleaseId,
    val timestamp: Date,
    val lastOpenTimestamp: Date
)