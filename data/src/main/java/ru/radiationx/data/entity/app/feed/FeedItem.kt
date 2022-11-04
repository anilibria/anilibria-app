package ru.radiationx.data.entity.app.feed

import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

data class FeedItem(
    val release: ReleaseItem? = null,
    val youtube: YoutubeItem? = null
)