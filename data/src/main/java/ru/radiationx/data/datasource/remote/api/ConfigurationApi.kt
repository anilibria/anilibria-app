package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.config.ApiConfigResponse
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val apiConfig: ApiConfig,
    private val apiConfigStorage: ApiConfigStorage,
    private val moshi: Moshi
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
        return response
            .fetchApiResponse<ApiConfigResponse>(moshi)
            .also { apiConfigStorage.save(it) }
            .toDomain()
            .also { apiConfig.setConfig(it) }
            .addresses
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
        .fetchResponse<ApiConfigResponse>(moshi)
        .also { apiConfigStorage.save(it) }
        .toDomain()
        .also { apiConfig.setConfig(it) }
        .addresses

}