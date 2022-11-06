package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.entity.response.search.SuggestionResponse
import javax.inject.Inject

class SearchApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getGenres(): List<String> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "genres"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<String>(moshi)
    }

    suspend fun getYears(): List<String> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "years"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<String>(moshi)
    }

    suspend fun fastSearch(name: String): List<SuggestionResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "search",
            "search" to name,
            "filter" to "id,code,names,poster"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<SuggestionResponse>(moshi)
    }

    suspend fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        complete: String,
        page: Int
    ): PaginatedResponse<ReleaseResponse> {
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
            .fetchPaginatedApiResponse<ReleaseResponse>(moshi)
    }

}
