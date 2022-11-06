package ru.radiationx.data.entity.domain.feed

import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.youtube.YoutubeItem

data class FeedItem(
    val release: Release? = null,
    val youtube: YoutubeItem? = null
)