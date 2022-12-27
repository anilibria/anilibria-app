package ru.radiationx.data.entity.domain.feed

import ru.radiationx.data.entity.domain.release.Release

data class ScheduleItem(
    val releaseItem: Release,
    val completed: Boolean = false
)