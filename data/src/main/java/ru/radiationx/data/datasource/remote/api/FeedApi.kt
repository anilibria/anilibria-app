package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.entity.response.feed.FeedResponse
import javax.inject.Inject

class FeedApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getFeed(page: Int): List<FeedResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "feed",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,externalPlaylist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse(moshi)
    }

}