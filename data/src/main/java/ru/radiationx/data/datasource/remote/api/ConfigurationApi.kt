package ru.radiationx.data.datasource.remote.api

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.ConfigurationParser
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val configurationParser: ConfigurationParser,
    private val apiConfig: ApiConfig,
    private val apiConfigStorage: ApiConfigStorage,
) {

    suspend fun checkAvailable(apiUrl: String): Boolean {
        return withTimeout(15_000) {
            check(mainClient, apiUrl)
        }
    }

    suspend fun checkApiAvailable(apiUrl: String): Boolean {
        return withTimeout(15_000) {
            try {
                check(client, apiUrl)
            } catch (ex: Throwable) {
                false
            }
        }
    }

    suspend fun getConfiguration(): List<ApiAddress> {
        return getMergeConfig().also {
            if (it.isEmpty()) {
                throw IllegalStateException("Empty config adresses")
            }
        }
    }

    private suspend fun check(client: IClient, apiUrl: String): Boolean {
        return client
            .postFull(apiUrl, mapOf("query" to "empty"))
            .let { true }
    }

    private suspend fun getMergeConfig(): List<ApiAddress> {
        val apiFlow = flow {
            emit(getConfigFromApi())
        }.catch {
            emit(emptyList())
        }
        val reserveFlow = flow {
            emit(getConfigFromReserve())
        }.catch {
            emit(emptyList())
        }
        return merge(apiFlow, reserveFlow)
            .filter { it.isNotEmpty() }
            .onEmpty { emit(emptyList()) }
            .first()
    }

    private suspend fun getConfigFromApi(): List<ApiAddress> {
        val args = mapOf(
            "query" to "config"
        )
        val response = withTimeout(10_000) {
            client.post(apiConfig.apiUrl, args)
        }
        return response.fetchResult<JSONObject>()
            .also { apiConfigStorage.saveJson(it) }
            .let { configurationParser.parse(it) }
            .also { apiConfig.setAddresses(it) }
    }

    private suspend fun getConfigFromReserve(): List<ApiAddress> {
        return try {
            getReserve("https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json")
        } catch (ex: Throwable) {
            getReserve("https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json")
        }
    }

    private suspend fun getReserve(url: String): List<ApiAddress> = mainClient
        .get(url, emptyMap())
        .let { JSONObject(it) }
        .also { apiConfigStorage.saveJson(it) }
        .let { configurationParser.parse(it) }
        .also { apiConfig.setAddresses(it) }

}