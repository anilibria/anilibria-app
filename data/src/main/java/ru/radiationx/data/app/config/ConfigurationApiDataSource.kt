package ru.radiationx.data.app.config

import kotlinx.coroutines.withTimeout
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.config.models.ApiAddress
import ru.radiationx.data.app.config.remote.ApiConfigResponse
import ru.radiationx.data.common.Url
import ru.radiationx.shared.ktx.parallelFirstNotFailure
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class ConfigurationApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val configUrls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/config-v2.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/config-v2.json"
    )

    suspend fun findFastest(addresses: List<ApiAddress>): ApiAddress {
        return addresses.parallelFirstNotFailure { address ->
            withTimeout(15_000) {
                api.checkUrl(address.status.value)
            }
            address
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