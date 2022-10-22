package ru.radiationx.data.datasource.remote.api

import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject

class FavoriteApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getFavorites(page: Int): Paginated<List<ReleaseItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.releases(it) }
    }

    suspend fun addFavorite(releaseId: Int): ReleaseItem {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "add",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.release(it) }
    }

    suspend fun deleteFavorite(releaseId: Int): ReleaseItem {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "delete",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.release(it) }
    }

}