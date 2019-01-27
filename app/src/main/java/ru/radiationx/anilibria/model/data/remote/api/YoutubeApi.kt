package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import ru.radiationx.anilibria.model.data.remote.parsers.YoutubeParser

class YoutubeApi(
        private val client: IClient,
        private val youtubeParser: YoutubeParser
) {

    fun getYoutubeList(page: Int): Single<Paginated<List<YoutubeItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "youtube",
                "page" to page.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { youtubeParser.parse(it) }
    }
}