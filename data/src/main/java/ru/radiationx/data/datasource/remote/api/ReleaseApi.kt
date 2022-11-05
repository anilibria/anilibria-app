package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.release.RandomReleaseResponse
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

/* Created by radiationx on 31.10.17. */

class ReleaseApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi,
    private val apiUtils: ApiUtils
) {

    suspend fun getRandomRelease(): RandomRelease {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "random_release"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<RandomReleaseResponse>(moshi)
            .toDomain()
    }

    suspend fun getRelease(releaseId: Int): Release {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<ReleaseResponse>(moshi)
            .toDomain(apiUtils, apiConfig)
    }

    suspend fun getRelease(releaseCode: String): Release {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "code" to releaseCode
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<ReleaseResponse>(moshi)
            .toDomain(apiUtils, apiConfig)
    }

    suspend fun getReleasesByIds(ids: List<Int>): List<Release> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "info",
            "id" to ids.joinToString(","),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse<ReleaseResponse>(moshi)
            .map { it.toDomain(apiUtils, apiConfig) }
    }

    suspend fun getReleases(page: Int): Paginated<Release> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "list",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchPaginatedApiResponse<ReleaseResponse>(moshi)
            .toDomain { it.toDomain(apiUtils, apiConfig) }
    }


}

        