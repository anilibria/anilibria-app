package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.CheckerParser

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi(
        private val client: IClient,
        apiUtils: IApiUtils
) {

    private val checkerParser = CheckerParser(apiUtils)

    fun checkUpdate(versionCode: Int): Single<UpdateData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "app_v2",
                "check" to "update",
                "current" to versionCode.toString()
        )
        return client.post(Api.API_URL, args)
                .map { checkerParser.parse(it) }
    }

    fun checkUpdateFromRepository(): Single<UpdateData> {
        return client.get("https://bitbucket.org/RadiationX/anilibria-app/raw/master/check.json", emptyMap())
                .map { checkerParser.parse(it) }
    }

}