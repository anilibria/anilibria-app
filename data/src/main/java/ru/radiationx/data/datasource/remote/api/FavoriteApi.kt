package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.data.entity.response.release.ReleaseResponse
import javax.inject.Inject

class FavoriteApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getFavorites(page: Int): PaginatedResponse<ReleaseResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchPaginatedApiResponse(moshi)
    }

    suspend fun addFavorite(releaseId: Int): ReleaseResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "add",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

    suspend fun deleteFavorite(releaseId: Int): ReleaseResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "delete",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

}