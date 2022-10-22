package ru.radiationx.data.datasource.remote.api

import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.YoutubeParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import javax.inject.Inject

class YoutubeApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val youtubeParser: YoutubeParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getYoutubeList(page: Int): Paginated<List<YoutubeItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "youtube",
            "page" to page.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { youtubeParser.parse(it) }
    }
}