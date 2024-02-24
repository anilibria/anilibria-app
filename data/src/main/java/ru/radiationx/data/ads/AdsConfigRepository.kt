package ru.radiationx.data.ads

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.ads.domain.AdsConfig
import ru.radiationx.data.ads.domain.BannerAdConfig
import ru.radiationx.data.ads.domain.NativeAdConfig
import ru.radiationx.data.ads.remote.AdsConfigResponse
import ru.radiationx.data.interactors.SharedRequests
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AdsConfigRepository @Inject constructor(
    private val api: AdsConfigApi,
    private val storage: AdsConfigStorage,
    private val buildConfig: SharedBuildConfig,
) {

    private val default = listOf(
        AdsConfig(
            appId = "ru.radiationx.anilibria.app",
            mainBanner = BannerAdConfig(
                enabled = true,
                unitId = "R-M-4562617-1",
                emptyList()
            ),
            feedNative = NativeAdConfig(
                enabled = true,
                unitId = "R-M-4562617-2",
                timeoutMillis = 2500,
                contextTags = emptyList(),
                listInsertPosition = 1
            ),
            releaseNative = NativeAdConfig(
                enabled = true,
                unitId = "R-M-4562617-2",
                timeoutMillis = 2500,
                contextTags = emptyList(),
                listInsertPosition = 0
            )
        )
    )

    private val disabledConfig = AdsConfig(
        appId = buildConfig.applicationId,
        mainBanner = BannerAdConfig(
            enabled = false,
            unitId = "",
            emptyList()
        ),
        feedNative = NativeAdConfig(
            enabled = false,
            unitId = "",
            timeoutMillis = 0,
            contextTags = emptyList(),
            listInsertPosition = 0
        ),
        releaseNative = NativeAdConfig(
            enabled = false,
            unitId = "",
            timeoutMillis = 0,
            contextTags = emptyList(),
            listInsertPosition = 0
        )
    )

    private var wasLoadAttempt = false

    private val requests = SharedRequests<String, List<AdsConfigResponse>>()

    suspend fun getConfig(): AdsConfig {
        if (!buildConfig.hasAds) {
            return disabledConfig
        }
        val result = if (wasLoadAttempt) {
            storage.get()
        } else {
            val result = coRunCatching {
                loadConfig()
            }.onFailure {
                Timber.e(it,"Error while get config")
            }.getOrNull()
            result ?: storage.get()
        }
        wasLoadAttempt = true
        val configs = result?.map { it.toDomain() } ?: default
        val configByAppId = configs.find { it.appId == buildConfig.applicationId }
        return configByAppId ?: disabledConfig
    }

    private suspend fun loadConfig(): List<AdsConfigResponse> {
        return withContext(Dispatchers.IO) {
            val response = requests.request("ads") {
                api.getConfig()
            }
            storage.save(response)
            response
        }
    }
}