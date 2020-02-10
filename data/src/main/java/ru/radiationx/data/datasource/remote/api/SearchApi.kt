package ru.radiationx.data.datasource.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.datasource.remote.parsers.SearchParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SearchItem
import javax.inject.Inject

class SearchApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val searchParser: SearchParser,
        private val apiConfig: ApiConfig
) {

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "genres"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { searchParser.genres(it) }
    }

    fun getYears(): Single<List<YearItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "years"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { searchParser.years(it) }
    }

    fun fastSearch(name: String): Single<List<SearchItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "search",
                "search" to name,
                "filter" to "id,code,names,poster"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { searchParser.fastSearch(it) }
    }

    fun searchReleases(genre: String, year: String, season: String, sort: String, complete: String, page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "catalog",
                "search" to JSONObject().apply {
                    put("genre", genre)
                    put("year", year)
                    put("season", season)
                }.toString(),
                "finish" to complete,
                "xpage" to "catalog",
                "sort" to sort,
                "page" to page.toString(),
                "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
                "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.releases(it) }
    }

}
