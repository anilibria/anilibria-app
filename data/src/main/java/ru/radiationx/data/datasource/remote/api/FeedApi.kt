package ru.radiationx.data.datasource.remote.api

import org.json.JSONArray
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.FeedParser
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.datasource.remote.parsers.YoutubeParser
import ru.radiationx.data.entity.app.feed.FeedItem
import javax.inject.Inject

class FeedApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val youtubeParser: YoutubeParser,
    private val feedParser: FeedParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getFeed(page: Int): List<FeedItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "feed",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONArray>()
            .let { feedParser.feed(it, releaseParser, youtubeParser) }
    }

}