package ru.radiationx.anilibria.model.datasource.remote.api

import io.reactivex.Single
import org.json.JSONArray
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.data.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.model.datasource.remote.ApiResponse
import ru.radiationx.anilibria.model.datasource.remote.IClient
import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
import ru.radiationx.anilibria.model.datasource.remote.parsers.MenuParser
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
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { menuParse.parse(it) }
    }

}