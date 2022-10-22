package ru.radiationx.anilibria.model

data class FeedItemState(
    val release: ReleaseItemState?,
    val youtube: YoutubeItemState?
)