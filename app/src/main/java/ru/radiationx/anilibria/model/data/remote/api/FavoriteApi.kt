package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.release.FavoriteData
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.FavoriteParser
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser

class FavoriteApi(
        private val client: IClient,
        private val favoriteParser: FavoriteParser
) {

    fun getFavorites2(): Single<FavoriteData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "SHOWALL_1" to "1",
                "action" to "favorites"
        )
        return client.get(Api.API_URL, args)
                .map { favoriteParser.favorites2(it) }
    }

    fun deleteFavorite(id: Int, sessId: String): Single<FavoriteData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "SHOWALL_1" to "1",
                "action" to "favorites",
                "a" to "",
                "sessid" to sessId,
                "del" to id.toString()
        )
        return client.get(Api.API_URL, args)
                .map { favoriteParser.favorites2(it) }
    }

    fun sendFav(id: Int, isFaved: Boolean, sessId: String, sKey: String): Single<Int> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to if (isFaved) "like" else "unlike",
                "id" to id.toString(),
                "sessid" to sessId,
                "key" to sKey,
                "type" to "unknown"
        )
        return client.get("${Api.BASE_URL}/bitrix/tools/asd_favorite.php", args)
                .map { favoriteParser.favXhr(it) }
    }

}