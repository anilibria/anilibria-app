package ru.radiationx.data.app.updater

import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.updater.remote.UpdateDataRootResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApiDataSource @Inject constructor(
    private val reserveSources: CheckerReserveSources,
    private val api: DirectApi
) {

    suspend fun checkUpdate(): UpdateDataRootResponse {
        return reserveSources.sources.sequentialFirstNotFailure { url ->
            api.getUpdate(url)
        }
    }

}