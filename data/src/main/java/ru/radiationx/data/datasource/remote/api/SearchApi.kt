package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.mapper.toGenreItem
import ru.radiationx.data.entity.mapper.toYearItem
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.entity.response.search.SuggestionResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class SearchApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val apiUtils: ApiUtils,
    private val moshi: Moshi
) {

    suspend fun getGenres(): List<GenreItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "genres"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<String>(moshi)
            .map { it.toGenreItem() }
    }

    suspend fun getYears(): List<YearItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "years"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<String>(moshi)
            .map { it.toYearItem() }
    }

    suspend fun fastSearch(name: String): List<SuggestionItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "search",
            "search" to name,
            "filter" to "id,code,names,poster"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<SuggestionResponse>(moshi)
            .map { it.toDomain(apiUtils, apiConfig) }
    }

    suspend fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        complete: String,
        page: Int
    ): Paginated<Release> {
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
            .toDomain { it.toDomain(apiUtils, apiConfig) }
    }

}
