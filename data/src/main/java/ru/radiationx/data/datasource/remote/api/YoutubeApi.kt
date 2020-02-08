package ru.radiationx.data.datasource.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.YoutubeParser
import javax.inject.Inject

class YoutubeApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val youtubeParser: YoutubeParser,
        private val apiConfig: ApiConfig
) {

    fun getYoutubeList(page: Int): Single<Paginated<List<YoutubeItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "youtube",
                "page" to page.toString()
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { youtubeParser.parse(it) }
    }
}