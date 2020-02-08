package ru.radiationx.anilibria.model.datasource.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.datasource.remote.ApiResponse
import ru.radiationx.anilibria.model.datasource.remote.IClient
import ru.radiationx.anilibria.model.datasource.remote.address.ApiAddress
import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
import ru.radiationx.anilibria.model.datasource.remote.parsers.ConfigurationParser
import ru.radiationx.anilibria.model.datasource.storage.ApiConfigStorage
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
        @ApiClient private val client: IClient,
        @MainClient private val mainClient: IClient,
        private val configurationParser: ConfigurationParser,
        private val apiConfig: ApiConfig,
        private val apiConfigStorage: ApiConfigStorage
) {

    fun checkAvailable(apiUrl: String): Single<Boolean> = check(mainClient, apiUrl)

    fun checkApiAvailable(apiUrl: String): Single<Boolean> = check(client, apiUrl)
            .onErrorReturnItem(false)

    fun getConfiguration(): Single<List<ApiAddress>> {
        val args = mapOf(
                "query" to "config"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .doOnSuccess { apiConfigStorage.saveJson(it) }
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
                .onErrorResumeNext { getReserve("https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json") }
                .onErrorResumeNext { getReserve("https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json") }
    }

    private fun check(client: IClient, apiUrl: String): Single<Boolean> =
            client.postFull(apiUrl, mapOf("query" to "empty"))
                    .map {
                        /*val hostIp = it.hostIp.orEmpty()
                        if (!apiConfig.getPossibleIps().contains(it.hostIp)) {
                            throw WrongHostException(hostIp)
                        }*/
                        true
                    }

    private fun getReserve(url: String): Single<List<ApiAddress>> = mainClient.get(url, emptyMap())
            .map { JSONObject(it) }
            .doOnSuccess { apiConfigStorage.saveJson(it) }
            .map { configurationParser.parse(it) }
            .doOnSuccess { apiConfig.setAddresses(it) }

}