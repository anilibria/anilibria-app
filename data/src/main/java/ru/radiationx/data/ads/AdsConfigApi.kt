package ru.radiationx.data.ads

import ru.radiationx.data.ads.remote.AdsConfigDataResponse
import ru.radiationx.data.datasource.remote.api.DirectApi
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class AdsConfigApi @Inject constructor(
    private val api: DirectApi
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/adsconfig.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/adsconfig.json"
    )

    suspend fun getConfig(): AdsConfigDataResponse = urls.sequentialFirstNotFailure { url ->
        api.getAdsConfig(url)
    }
}