package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.domain.types.ReleaseId

data class ScheduleItemState(
    val releaseId: ReleaseId,
    val posterUrl: String,
    val isCompleted: Boolean
)