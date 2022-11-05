package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.feed.FeedResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FeedApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val apiUtils: ApiUtils,
    private val moshi: Moshi
) {

    suspend fun getFeed(page: Int): List<FeedItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "feed",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<FeedResponse>(moshi)
            .map { it.toDomain(apiUtils, apiConfig) }
    }

}