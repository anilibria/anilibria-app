package ru.radiationx.anilibria.model.data.remote.api

import com.yandex.metrica.YandexMetrica
import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.CheckerParser
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi @Inject constructor(
        @ApiClient private val client: IClient,
        @MainClient private val mainClient: IClient,
        private val checkerParser: CheckerParser,
        private val apiConfig: ApiConfig
) {

    fun checkUpdate(versionCode: Int): Single<UpdateData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "app_update",
                "current" to versionCode.toString()
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { checkerParser.parse(it) }
                .onErrorResumeNext { getReserve("https://github.com/anilibria/anilibria-app/blob/master/check.json") }
                .onErrorResumeNext { getReserve("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json") }
    }

    private fun getReserve(url: String): Single<UpdateData> = mainClient.get(url, emptyMap())
            .map { JSONObject(it) }
            .map { checkerParser.parse(it) }
}