package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FavoriteApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi,
    private val apiUtils: ApiUtils
) {

    suspend fun getFavorites(page: Int): Paginated<Release> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchPaginatedApiResponse<ReleaseResponse>(moshi)
            .toDomain { it.toDomain(apiUtils, apiConfig) }
    }

    suspend fun addFavorite(releaseId: Int): Release {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "add",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<ReleaseResponse>(moshi)
            .toDomain(apiUtils, apiConfig)
    }

    suspend fun deleteFavorite(releaseId: Int): Release {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "favorites",
            "action" to "delete",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<ReleaseResponse>(moshi)
            .toDomain(apiUtils, apiConfig)
    }

}