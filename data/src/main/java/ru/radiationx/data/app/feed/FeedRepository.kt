package ru.radiationx.data.app.feed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.catalog.CatalogApiDataSource
import ru.radiationx.data.app.feed.models.FeedItem
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import ru.radiationx.data.common.FeedId
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val catalogApi: CatalogApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    // todo API2 await feed api implementation and replace
    suspend fun getFeed(page: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        catalogApi.getReleases(page, null)
            .data
            .map {
                FeedItem(FeedId(it.id, null), it, null)
            }
            .also { updateMiddleware.handleFeed(it) }
    }

}