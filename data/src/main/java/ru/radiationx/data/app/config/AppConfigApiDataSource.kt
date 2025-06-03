package ru.radiationx.data.app.config

import kotlinx.coroutines.withTimeout
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.app.config.remote.AppConfigResponse
import ru.radiationx.shared.ktx.parallelFirstNotFailure
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class AppConfigApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val configUrls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/config-v2.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/config-v2.json"
    )

    suspend fun findFastest(addresses: List<AppConfigAddress>): AppConfigAddress {
        return addresses.parallelFirstNotFailure { address ->
            withTimeout(15_000) {
                api.checkUrl(address.status.value)
            }
            address
        }
    }

    suspend fun getConfig(): AppConfigResponse {
        return configUrls.sequentialFirstNotFailure { url ->
            val response = api.getAppConfig(url)
            check(response.addresses.isNotEmpty()) {
                "Config addresses is empty"
            }
            response
        }
    }

}