package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.YoutubeParser
import javax.inject.Inject

class YoutubeApi @Inject constructor(
        private val client: IClient,
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