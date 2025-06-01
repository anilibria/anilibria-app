package ru.radiationx.anilibria.model

import ru.radiationx.data.common.FeedId

data class FeedItemState(
    val id: FeedId,
    val release: ReleaseItemState?,
    val youtube: YoutubeItemState?
)