package ru.radiationx.data.app.config

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.app.config.remote.AppConfigResponse
import ru.radiationx.shared.ktx.parallelFirstNotFailure
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import ru.radiationx.shared.ktx.withTimeoutOrThrow
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.measureTimedValue

class AppConfigApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val configUrls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/config-v2.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/config-v2.json"
    )

    suspend fun checkWithTimeout(address: AppConfigAddress) {
        withTimeoutOrThrow(15_000) {
            api.checkUrl(address.status.value)
        }
    }

    suspend fun findFastest(addresses: List<AppConfigAddress>): AppConfigAddress {
        return addresses.parallelFirstNotFailure { address ->
            checkWithTimeout(address)
            address
        }
    }

    suspend fun findFastestDebug(addresses: List<AppConfigAddress>): AppConfigAddress {
        return supervisorScope {
            val deferred = addresses.map { address ->
                async {
                    address to measureTimedValue {
                        runCatching {
                            checkWithTimeout(address)
                        }
                    }
                }
            }
            val results = deferred.awaitAll()
            results.forEach { (address, timedResult) ->
                Timber.d("findFastestDebug ${address.id} -> ${timedResult.duration}, ${timedResult.value}")
            }
            results
                .filter { it.second.value.isSuccess }
                .minBy { it.second.duration }
                .first
        }
    }

    suspend fun getConfig(): AppConfigResponse {
        return configUrls.sequentialFirstNotFailure { url ->
            val response = withTimeoutOrThrow(5_000) {
                api.getAppConfig(url)
            }
            check(response.addresses.isNotEmpty()) {
                "Config addresses is empty"
            }
            response
        }
    }

}