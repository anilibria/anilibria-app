package ru.radiationx.data.interactors

import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseUpdateMiddleware(
    private val holder: ReleaseUpdateHolder
) {

    fun handle(releases: List<ReleaseItem>) {
        holder.putInitialRelease(releases)
    }

    fun handle(release: ReleaseItem) {
        holder.putInitialRelease(listOf(release))
    }

    fun handleFeed(feedItems: List<FeedItem>) {
        handle(feedItems.mapNotNull { it.release })
    }
}