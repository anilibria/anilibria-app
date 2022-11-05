package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.api.FeedApi
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    suspend fun getFeed(page: Int): List<FeedItem> = feedApi
        .getFeed(page)
        .also { updateMiddleware.handleFeed(it) }

}