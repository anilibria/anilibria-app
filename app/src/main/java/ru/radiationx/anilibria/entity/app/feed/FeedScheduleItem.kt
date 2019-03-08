package ru.radiationx.anilibria.entity.app.feed

import ru.radiationx.anilibria.entity.app.release.ReleaseItem

class FeedScheduleItem(
        val releaseItem: ReleaseItem,
        val completed: Boolean
)