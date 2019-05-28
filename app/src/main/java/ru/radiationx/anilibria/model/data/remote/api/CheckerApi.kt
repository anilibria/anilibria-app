package ru.radiationx.anilibria.model.data.remote.api

import com.yandex.metrica.YandexMetrica
import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.updater.UpdateData
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
        private val client: IClient,
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
                .doOnSuccess { saveAddresses(it) }
                .map { checkerParser.parse(it) }
    }

    fun checkUpdateFromRepository(): Single<UpdateData> {
        return client.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json", emptyMap())
                .map { JSONObject(it) }
                .doOnSuccess { saveAddresses(it) }
                .map { checkerParser.parse(it) }
    }

    private fun saveAddresses(jsonResponse: JSONObject) {
        try {
            checkerParser.parseAddresses(jsonResponse)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            YandexMetrica.reportError("${ex.message}", ex)
            null
        }?.also {
            apiConfig.setAddresses(it)
        }
    }

}