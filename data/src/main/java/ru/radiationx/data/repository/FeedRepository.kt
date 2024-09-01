package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.CatalogApiDataSource
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.types.FeedId
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val catalogApi: CatalogApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
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