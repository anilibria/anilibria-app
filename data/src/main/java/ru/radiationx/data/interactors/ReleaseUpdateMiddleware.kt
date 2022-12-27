package ru.radiationx.data.interactors

import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.release.Release
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseUpdateMiddleware(
    private val holder: ReleaseUpdateHolder
) {

    suspend fun handle(releases: List<Release>) {
        holder.putInitialRelease(releases)
    }

    suspend fun handle(release: Release) {
        holder.putInitialRelease(listOf(release))
    }

    suspend fun handleFeed(feedItems: List<FeedItem>) {
        handle(feedItems.mapNotNull { it.release })
    }
}