package ru.radiationx.data.datasource.remote.api

import io.reactivex.Single
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.VitalParser
import ru.radiationx.data.entity.app.vital.VitalItem
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class VitalApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val vitalParser: VitalParser,
        private val apiConfig: ApiConfig
) {

    fun loadVital(): Single<List<VitalItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "app_v2"
        )
        return client.post(apiConfig.apiUrl, args)
                .map { vitalParser.vital(it) }
    }
}