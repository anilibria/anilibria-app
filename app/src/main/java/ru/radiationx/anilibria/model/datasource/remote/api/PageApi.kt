package ru.radiationx.anilibria.model.datasource.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.anilibria.model.datasource.remote.ApiResponse
import ru.radiationx.anilibria.model.datasource.remote.IClient
import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
import ru.radiationx.anilibria.model.datasource.remote.parsers.PagesParser
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageApi @Inject constructor(
        @ApiClient private val client: IClient,
        private val pagesParser: PagesParser,
        private val apiConfig: ApiConfig
) {
    companion object {
        const val PAGE_PATH_TEAM = "pages/team.php"
        const val PAGE_PATH_DONATE = "pages/donate.php"

        val PAGE_IDS = listOf(
                PAGE_PATH_TEAM,
                PAGE_PATH_DONATE
        )
    }

    fun getPage(pagePath: String): Single<PageLibria> {
        val args: Map<String, String> = emptyMap()
        return client.get("${apiConfig.baseUrl}/$pagePath", args)
                .map { pagesParser.baseParse(it) }
    }

    fun getComments(): Single<VkComments> {
        val args: Map<String, String> = mapOf(
                "query" to "vkcomments"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { pagesParser.parseVkComments(it) }
    }
}