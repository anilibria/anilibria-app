package ru.radiationx.data.datasource.remote.api

import io.reactivex.Single
import org.json.JSONArray
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.MenuParser
import ru.radiationx.data.entity.app.other.LinkMenuItem
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