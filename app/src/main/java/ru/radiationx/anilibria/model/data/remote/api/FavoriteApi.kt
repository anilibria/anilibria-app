package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
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