package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.response.config.ApiConfigResponse
import javax.inject.Inject

class ConfigurationApi @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi,
) {

    suspend fun checkAvailable(apiUrl: String): Boolean {
        return withTimeout(15_000) {
            mainClient
                .postFull(apiUrl, mapOf("query" to "empty"))
                .let { true }
        }
    }

    suspend fun getConfiguration(): ApiConfigResponse {
        return getMergeConfig().also {
            if (it.addresses.isEmpty()) {
                throw IllegalStateException("Empty config adresses")
            }
        }
    }

    private suspend fun getMergeConfig(): ApiConfigResponse {
        val apiFlow = flow {
            emit(getConfigFromApi())
        }.catch {
            emit(ApiConfigResponse(emptyList()))
        }
        val reserveFlow = flow {
            emit(getConfigFromReserve())
        }.catch {
            emit(ApiConfigResponse(emptyList()))
        }
        return merge(apiFlow, reserveFlow)
            .filter { it.addresses.isNotEmpty() }
            .onEmpty { emit(ApiConfigResponse(emptyList())) }
            .first()
    }

    private suspend fun getConfigFromApi(): ApiConfigResponse {
        val args = mapOf(
            "query" to "config"
        )
        val response = withTimeout(10_000) {
            mainClient.post(Api.DEFAULT_ADDRESS.api, args)
        }
        return response
            .fetchApiResponse(moshi)
    }

    private suspend fun getConfigFromReserve(): ApiConfigResponse {
        return try {
            getReserve("https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json")
        } catch (ex: Throwable) {
            getReserve("https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json")
        }
    }

    private suspend fun getReserve(url: String): ApiConfigResponse = mainClient
        .get(url, emptyMap())
        .fetchResponse(moshi)

}