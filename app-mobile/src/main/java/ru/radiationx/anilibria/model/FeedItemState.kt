package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.domain.types.FeedId

data class FeedItemState(
    val id: FeedId,
    val release: ReleaseItemState?,
    val youtube: YoutubeItemState?
)