package ru.radiationx.data.datasource.remote.api

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject

/* Created by radiationx on 31.10.17. */

class ReleaseApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getRandomRelease(): RandomRelease {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "random_release"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.parseRandomRelease(it) }
    }

    suspend fun getRelease(releaseId: Int): ReleaseFull {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "id" to releaseId.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.release(it) }
    }

    suspend fun getRelease(releaseCode: String): ReleaseFull {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "release",
            "code" to releaseCode
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.release(it) }
    }

    suspend fun getReleasesByIds(ids: List<Int>): List<ReleaseItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "info",
            "id" to ids.joinToString(","),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONArray>()
            .let { releaseParser.releases(it) }
    }

    suspend fun getReleases(page: Int): Paginated<List<ReleaseItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "list",
            "page" to page.toString(),
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { releaseParser.releases(it) }
    }


}

        