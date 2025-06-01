package ru.radiationx.data.datasource.remote.api

import kotlinx.coroutines.withTimeout
import ru.radiationx.data.entity.response.config.ApiConfigResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class ConfigurationApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val configUrls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/config.json"
    )

    suspend fun checkAvailable(apiUrl: String): Boolean {
        return withTimeout(15_000) {
            api.checkUrl(apiUrl)
            true
        }
    }

    suspend fun getConfiguration(): ApiConfigResponse {
        return configUrls.sequentialFirstNotFailure { url ->
            val response = api.getApiConfig(url)
            check(response.addresses.isNotEmpty()) {
                "Config addresses is empty"
            }
            response
        }
    }

}