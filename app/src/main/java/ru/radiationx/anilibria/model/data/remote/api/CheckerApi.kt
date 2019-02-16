package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.CheckerParser

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi(
        private val client: IClient,
        private val checkerParser: CheckerParser
) {

    fun checkUpdate(versionCode: Int): Single<UpdateData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "app_update",
                "current" to versionCode.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { checkerParser.parse(it) }
    }

    fun checkUpdateFromRepository(): Single<UpdateData> {
        return client.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json", emptyMap())
                .map { checkerParser.parse(JSONObject(it)) }
    }

    fun checkUnderAntiDdos(): Single<String> {
        val args: MutableMap<String, String> = mutableMapOf("query" to "empty")
        return client.post(Api.API_URL, args)
    }

}