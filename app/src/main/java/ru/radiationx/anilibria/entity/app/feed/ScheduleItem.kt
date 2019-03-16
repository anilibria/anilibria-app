package ru.radiationx.anilibria.entity.app.feed

import ru.radiationx.anilibria.entity.app.release.ReleaseItem

data class ScheduleItem(
        val releaseItem: ReleaseItem,
        val completed: Boolean = false
)