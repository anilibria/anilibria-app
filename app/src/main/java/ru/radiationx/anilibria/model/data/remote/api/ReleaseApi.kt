package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser

/* Created by radiationx on 31.10.17. */

class ReleaseApi(
        private val client: IClient,
        private val releaseParser: ReleaseParser
) {

    fun getRelease(releaseId: Int): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "release",
                "id" to releaseId.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.release(it) }
    }

    fun getRelease(releaseCode: String): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "release",
                "code" to releaseCode
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.release(it) }
    }

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "list",
                "page" to page.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { releaseParser.releases(it) }
    }


}

        