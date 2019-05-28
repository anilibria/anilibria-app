package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiAddress
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.ConfigurationParser
import ru.radiationx.anilibria.model.system.WrongHostException
import java.net.InetAddress
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
        @ApiClient private val client: IClient,
        @MainClient private val mainClient: IClient,
        private val configurationParser: ConfigurationParser,
        private val apiConfig: ApiConfig
) {

    fun checkAvailable(apiUrl: String): Single<Boolean> {
        val args = mapOf(
                "query" to "empty"
        )
        return client.post(apiUrl, args)
                .compose(ApiResponse.fetchResult<Any>())
                .doOnSuccess {
                    throw WrongHostException("allalalla")
                }
                .map { true }
                .onErrorReturn {
                    if (it is WrongHostException) {
                        false
                    } else {
                        throw it
                    }
                }
    }

    fun getConfiguration(): Single<List<ApiAddress>> {
        val args = mapOf(
                "query" to "configuration"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
                .onErrorReturn { emptyList() }
    }

    fun getReserve(): Single<List<ApiAddress>> {
        return mainClient.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json", emptyMap())
                .map { JSONObject(it) }
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
    }

}