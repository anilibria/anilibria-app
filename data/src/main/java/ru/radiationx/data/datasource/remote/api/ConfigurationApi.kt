package ru.radiationx.data.datasource.remote.api

import android.util.Log
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.ConfigurationParser
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val configurationParser: ConfigurationParser,
    private val apiConfig: ApiConfig,
    private val apiConfigStorage: ApiConfigStorage,
    private val schedulers: SchedulersProvider
) {

    fun checkAvailable(apiUrl: String): Single<Boolean> = check(mainClient, apiUrl)
        .timeout(15, TimeUnit.SECONDS)

    fun checkApiAvailable(apiUrl: String): Single<Boolean> = check(client, apiUrl)
        .onErrorReturnItem(false)
        .timeout(15, TimeUnit.SECONDS)

    fun getConfiguration(): Single<List<ApiAddress>> = getMergeConfig()
        .doOnSuccess {
            if (it.isEmpty()) {
                throw IllegalStateException("Empty config adresses")
            }
        }

    private fun check(client: IClient, apiUrl: String): Single<Boolean> =
        client.postFull(apiUrl, mapOf("query" to "empty"))
            .map { true }


    private fun getMergeConfig(): Single<List<ApiAddress>> = Single
        .merge(
            getConfigFromApi()
                .subscribeOn(schedulers.io())
                .onErrorReturn { emptyList() },
            getConfigFromReserve()
                .subscribeOn(schedulers.io())
                .onErrorReturn { emptyList() }
        )
        .filter { it.isNotEmpty() }
        .first(emptyList())

    private fun getZipConfig(): Single<List<ApiAddress>> = Single
        .zip(
            getConfigFromApi()
                .subscribeOn(schedulers.io())
                .onErrorReturn { emptyList() },
            getConfigFromReserve()
                .subscribeOn(schedulers.io())
                .onErrorReturn { emptyList() },
            BiFunction<List<ApiAddress>, List<ApiAddress>, List<ApiAddress>> { conf1, conf2 ->
                val addresses1 = conf1.takeIf { it.isNotEmpty() }
                val addresses2 = conf2.takeIf { it.isNotEmpty() }
                return@BiFunction (addresses1 ?: addresses2).orEmpty()
            }
        )


    private fun getConfigFromApi(): Single<List<ApiAddress>> {
        val args = mapOf(
            "query" to "config"
        )
        return client.post(apiConfig.apiUrl, args)
            .timeout(10, TimeUnit.SECONDS)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .doOnSuccess { apiConfigStorage.saveJson(it) }
            .map { configurationParser.parse(it) }
            .doOnSuccess { apiConfig.setAddresses(it) }
    }

    private fun getConfigFromReserve(): Single<List<ApiAddress>> {
        return getReserve("https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json")
            .onErrorResumeNext { getReserve("https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json") }
    }

    private fun getReserve(url: String): Single<List<ApiAddress>> = mainClient.get(url, emptyMap())
        .map { JSONObject(it) }
        .doOnSuccess { apiConfigStorage.saveJson(it) }
        .map { configurationParser.parse(it) }
        .doOnSuccess { apiConfig.setAddresses(it) }

}