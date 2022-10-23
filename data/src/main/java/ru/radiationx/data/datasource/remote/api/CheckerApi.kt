package ru.radiationx.data.datasource.remote.api

import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.CheckerParser
import ru.radiationx.data.entity.app.updater.UpdateData
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val checkerParser: CheckerParser,
    private val apiConfig: ApiConfig,
    private val reserveSources: CheckerReserveSources
) {

    suspend fun checkUpdate(versionCode: Int): UpdateData {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "app_update",
            "current" to versionCode.toString()
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchResult<JSONObject>()
                .let { checkerParser.parse(it) }
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
        .let { JSONObject(it) }
        .let { checkerParser.parse(it) }
}