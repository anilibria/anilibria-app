package ru.radiationx.data.datasource.remote.api

import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.entity.response.updater.UpdateDataRootResponse
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