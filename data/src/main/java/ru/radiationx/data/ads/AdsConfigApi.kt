package ru.radiationx.data.ads

import com.squareup.moshi.Moshi
import ru.radiationx.data.MainClient
import ru.radiationx.data.ads.remote.AdsConfigDataResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchResponse
import timber.log.Timber
import javax.inject.Inject

class AdsConfigApi @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val moshi: Moshi,
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/adsconfig.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/adsconfig.json"
    )

    suspend fun getConfig(): AdsConfigDataResponse {
        urls.forEach { url ->
            try {
                return mainClient
                    .get(url, emptyMap())
                    .fetchResponse(moshi)
            } catch (ex: Throwable) {
                Timber.e(ex,"Error while load adsconfig by $url")
            }
        }
        throw IllegalStateException("Not found any valid ads config")
    }

}