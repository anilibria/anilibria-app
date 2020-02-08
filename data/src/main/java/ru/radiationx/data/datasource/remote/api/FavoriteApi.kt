package ru.radiationx.data.datasource.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import javax.inject.Inject

class FavoriteApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val apiConfig: ApiConfig
) {

    fun getFavorites(page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "favorites",
                "page" to page.toString(),
                "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
                "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.releases(it) }
    }

    fun addFavorite(releaseId: Int): Single<ReleaseItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "favorites",
                "action" to "add",
                "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.release(it) }
    }

    fun deleteFavorite(releaseId: Int): Single<ReleaseItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "favorites",
                "action" to "delete",
                "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.release(it) }
    }

}