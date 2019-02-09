package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.VitalParser
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class VitalApi @Inject constructor(
        private val client: IClient,
        private val vitalParser: VitalParser
) {

    fun loadVital(): Single<List<VitalItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "app_v2"
        )
        return client.post(Api.API_URL, args)
                .map { vitalParser.vital(it) }
    }
}