package ru.radiationx.data.ads

import com.squareup.moshi.Moshi
import ru.radiationx.data.MainClient
import ru.radiationx.data.ads.remote.AdsConfigResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.datasource.remote.fetchListResponse
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.response.config.ApiConfigResponse
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

    suspend fun getConfig(): List<AdsConfigResponse> {
        urls.forEach { url ->
            try {
                return mainClient
                    .get(url, emptyMap())
                    .fetchListResponse(moshi)
            } catch (ex: Throwable) {
                Timber.e(ex,"Error while load adsconfig by $url")
            }
        }
        throw IllegalStateException("Not found any valid ads config")
    }

}