package ru.radiationx.data.api.schedule.models

import ru.radiationx.data.api.releases.models.Release

data class ScheduleItem(
    val releaseItem: Release,
    val completed: Boolean = false
)