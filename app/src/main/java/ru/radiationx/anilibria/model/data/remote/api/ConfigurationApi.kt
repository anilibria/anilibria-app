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
        return mainClient.postFull(apiUrl, args)
                .doOnSuccess {
                    val hostIp = it.hostIp.orEmpty()
                    if (!apiConfig.getPossibleIps().contains(it.hostIp)) {
                        throw WrongHostException(hostIp)
                    }
                }
                .doOnSuccess {
                    throw Exception("allalalla")
                }
                .map { true }
                .onErrorReturn {
                    Log.d("bobobo", "error ${it.message}")
                    false
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