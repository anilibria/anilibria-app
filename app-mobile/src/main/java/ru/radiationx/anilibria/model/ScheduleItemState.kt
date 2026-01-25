package ru.radiationx.anilibria.model

data class ScheduleItemState(
    val release: ReleaseItemState,
    val isCompleted: Boolean,
)