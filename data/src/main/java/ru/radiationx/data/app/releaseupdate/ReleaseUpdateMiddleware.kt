package ru.radiationx.data.app.releaseupdate

import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.feed.models.FeedItem
import javax.inject.Inject

class ReleaseUpdateMiddleware @Inject constructor(
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