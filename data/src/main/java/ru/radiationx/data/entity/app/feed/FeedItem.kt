package ru.radiationx.data.entity.app.feed

import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.app.youtube.YoutubeItem

data class FeedItem(
    val release: Release? = null,
    val youtube: YoutubeItem? = null
)