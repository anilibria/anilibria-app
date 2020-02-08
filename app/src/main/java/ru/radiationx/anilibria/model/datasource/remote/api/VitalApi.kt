package ru.radiationx.anilibria.model.datasource.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.datasource.remote.IClient
import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
import ru.radiationx.anilibria.model.datasource.remote.parsers.VitalParser
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