package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.data.entity.response.release.RandomReleaseResponse
import ru.radiationx.data.entity.response.release.ReleaseResponse
import javax.inject.Inject

/* Created by radiationx on 31.10.17. */

class ReleaseApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getRandomRelease(): RandomReleaseResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "random_release"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

    suspend fun getRelease(releaseId: Int): ReleaseResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

    suspend fun getRelease(releaseCode: String): ReleaseResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "code" to releaseCode
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

    suspend fun getReleasesByIds(ids: List<Int>): List<ReleaseResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "info",
            "id" to ids.joinToString(","),
            "filter" to "id,torrents,playlist,externalPlaylist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse(moshi)
    }

    suspend fun getReleases(page: Int): PaginatedResponse<ReleaseResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "list",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,externalPlaylist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchPaginatedApiResponse(moshi)
    }


}

        