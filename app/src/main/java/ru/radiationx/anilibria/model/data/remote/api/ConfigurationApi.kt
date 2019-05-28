package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiAddress
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.ConfigurationParser
import ru.radiationx.anilibria.model.system.WrongHostException
import java.net.InetAddress
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
        private val client: IClient,
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
                    //throw WrongHostException("allalalla")
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
    }

    fun getReserve(): Single<List<ApiAddress>> {
        return client.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json", emptyMap())
                .map { JSONObject(it) }
                .map { configurationParser.parse(it) }
                .doOnSuccess { apiConfig.setAddresses(it) }
    }

}