package ru.radiationx.data.repository

import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.remote.api.FeedApi
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.ReleaseUpdate
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val schedulers: SchedulersProvider,
    private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    suspend fun getFeed(page: Int): List<FeedItem> = feedApi
        .getFeed(page)
        .also {
            val newItems = mutableListOf<ReleaseItem>()
            val updItems = mutableListOf<ReleaseUpdate>()
            it.filterNot { it.release == null }.map { it.release!! }.forEach { item ->
                val updItem = releaseUpdateHolder.getRelease(item.id)
                if (updItem == null) {
                    newItems.add(item)
                } else {
                    item.isNew =
                        item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                }
            }
            releaseUpdateHolder.putAllRelease(newItems)
            releaseUpdateHolder.updAllRelease(updItems)
        }

}