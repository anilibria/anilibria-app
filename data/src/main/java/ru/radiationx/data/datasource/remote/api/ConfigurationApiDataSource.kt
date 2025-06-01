package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.response.config.ApiConfigResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class ConfigurationApiDataSource @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val moshi: Moshi,
) {

    private val configUrls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json"
    )

    suspend fun checkAvailable(apiUrl: String): Boolean {
        return withTimeout(15_000) {
            mainClient
                .postFull(apiUrl, mapOf("query" to "empty"))
                .let { true }
        }
    }

    suspend fun getConfiguration(): ApiConfigResponse {
        return configUrls.sequentialFirstNotFailure { url ->
            val response = mainClient
                .get(url, emptyMap())
                .fetchResponse<ApiConfigResponse>(moshi)
            check(response.addresses.isNotEmpty()) {
                "Config addresses is empty"
            }
            response
        }
    }

}