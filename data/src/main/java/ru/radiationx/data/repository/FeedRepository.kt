package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.FeedApi
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    suspend fun getFeed(page: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        feedApi
            .getFeed(page)
            .map { it.toDomain(apiUtils, apiConfig) }
            .also { updateMiddleware.handleFeed(it) }
    }

}