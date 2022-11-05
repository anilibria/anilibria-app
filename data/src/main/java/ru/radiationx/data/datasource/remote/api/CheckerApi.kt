package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.updater.UpdateDataResponse
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val apiConfig: ApiConfig,
    private val reserveSources: CheckerReserveSources,
    private val moshi: Moshi
) {

    suspend fun checkUpdate(versionCode: Int): UpdateData {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "app_update",
            "current" to versionCode.toString()
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchApiResponse<UpdateDataResponse>(moshi)
                .toDomain()
        } catch (ex: Throwable) {
            reserveSources.sources.forEach { url ->
                runCatching {
                    getReserve(url)
                }.onSuccess {
                    return it
                }
            }
            throw ex
        }
    }

    private suspend fun getReserve(url: String): UpdateData = mainClient
        .get(url, emptyMap())
        .fetchResponse<UpdateDataResponse>(moshi)
        .toDomain()
}