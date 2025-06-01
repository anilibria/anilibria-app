package ru.radiationx.data.app.feed.models

import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.videos.models.YoutubeItem
import ru.radiationx.data.common.FeedId

data class FeedItem(
    val id: FeedId,
    val release: Release?,
    val youtube: YoutubeItem?
)