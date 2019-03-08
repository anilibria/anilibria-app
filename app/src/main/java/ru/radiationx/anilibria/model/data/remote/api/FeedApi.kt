package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.FeedParser
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import ru.radiationx.anilibria.model.data.remote.parsers.YoutubeParser
import javax.inject.Inject

class FeedApi @Inject constructor(
        private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val youtubeParser: YoutubeParser,
        private val feedParser: FeedParser
) {

    fun getFeed(page: Int): Single<List<FeedItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "feed",
                "page" to page.toString(),
                "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
                "rm" to "true"
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { feedParser.feed(it, releaseParser, youtubeParser) }
    }

}