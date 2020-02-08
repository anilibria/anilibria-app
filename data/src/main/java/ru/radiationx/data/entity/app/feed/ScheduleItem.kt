package ru.radiationx.data.entity.app.feed

import ru.radiationx.data.entity.app.release.ReleaseItem

data class ScheduleItem(
        val releaseItem: ReleaseItem,
        val completed: Boolean = false
)