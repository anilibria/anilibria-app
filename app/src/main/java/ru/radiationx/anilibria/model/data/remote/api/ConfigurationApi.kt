package ru.radiationx.anilibria.model.data.remote.api

import android.util.Log
import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiAddress
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.ConfigurationParser
import ru.radiationx.anilibria.model.data.storage.ApiConfigStorage
import ru.radiationx.anilibria.model.system.WrongHostException
import java.net.InetAddress
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
        @ApiClient private val client: IClient,
        @MainClient private val mainClient: IClient,
        private val configurationParser: ConfigurationParser,
        private val apiConfig: ApiConfig,
        private val apiConfigStorage: ApiConfigStorage
) {

    fun checkAvailable(apiUrl: String): Single<Boolean> {
        val args = mapOf(
                "query" to "empty"
        )
        return mainClient.postFull(apiUrl, args)
                .doOnSuccess {
                    val hostIp = it.hostIp.orEmpty()
                    if (!apiConfig.getPossibleIps().contains(it.hostIp)) {
                        throw WrongHostException(hostIp)
                    }
                }
                .map { true }
                .onErrorReturn { false }
    }

    fun getConfiguration(): Single<List<ApiAddress>> {
        val args = mapOf(
                "query" to "config"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .doOnSuccess { apiConfigStorage.saveJson(it) }
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
                .onErrorResumeNext { getReserve() }
    }

    private fun getReserve(): Single<List<ApiAddress>> {
        return mainClient.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json", emptyMap())
                .map { JSONObject(it) }
                .doOnSuccess { apiConfigStorage.saveJson(it) }
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
    }

}