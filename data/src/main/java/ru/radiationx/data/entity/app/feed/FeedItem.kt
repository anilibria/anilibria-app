package ru.radiationx.data.entity.app.feed

import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

class FeedItem(
        var release: ReleaseItem? = null,
        var youtube: YoutubeItem? = null
)