package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import ru.radiationx.anilibria.model.data.remote.parsers.SearchParser

class SearchApi(
        private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val searchParser: SearchParser
) {

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "genres"
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { searchParser.genres(it) }
    }

    fun fastSearch(name: String): Single<List<SearchItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "fast-search",
                "name" to name
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { searchParser.fastSearch(it) }
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "search",
                "genres" to genre,
                "years" to "",
                "name" to name,
                "page" to page.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.releases(it) }
    }

}
