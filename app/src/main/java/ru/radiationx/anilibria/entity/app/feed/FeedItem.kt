package ru.radiationx.anilibria.entity.app.feed

import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem

class FeedItem(
        var release: ReleaseItem? = null,
        var youtube: YoutubeItem? = null
)