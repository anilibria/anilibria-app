package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.response.updater.UpdateDataRootResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val reserveSources: CheckerReserveSources,
    private val moshi: Moshi
) {

    suspend fun checkUpdate(): UpdateDataRootResponse {
        return reserveSources.sources.sequentialFirstNotFailure {
            mainClient
                .get(it, emptyMap())
                .fetchResponse(moshi)
        }
    }

}