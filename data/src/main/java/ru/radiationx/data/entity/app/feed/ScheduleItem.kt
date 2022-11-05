package ru.radiationx.data.entity.app.feed

import ru.radiationx.data.entity.app.release.Release

data class ScheduleItem(
    val releaseItem: Release,
    val completed: Boolean = false
)