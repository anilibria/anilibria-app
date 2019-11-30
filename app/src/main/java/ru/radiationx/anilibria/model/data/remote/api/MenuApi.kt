package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.MenuParser
import ru.radiationx.anilibria.model.data.remote.parsers.PagesParser
import javax.inject.Inject

class MenuApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val menuParse: MenuParser,
        private val apiConfig: ApiConfig
) {

    fun getMenu(): Single<List<LinkMenuItem>> {
        val args: Map<String, String> = mapOf(
                "query" to "link_menu"
        )
        return client.get(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { menuParse.parse(it) }
    }

}