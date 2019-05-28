package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.entity.app.page.VkComments
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.PagesParser
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageApi @Inject constructor(
        private val client: IClient,
        private val pagesParser: PagesParser,
        private val apiConfig: ApiConfig
) {
    companion object {
        const val PAGE_ID_TEAM = "pages/team.php"
        const val PAGE_ID_BID = "zayavka-v-komandu.php"
        const val PAGE_ID_DONATE = "pages/donate.php"
        const val PAGE_ID_ABOUT_ANILIB = "anilibria.php"
        const val PAGE_ID_RULES = "pravila.php"

        val PAGE_IDS = listOf(
                PAGE_ID_TEAM,
                //PAGE_ID_BID,
                PAGE_ID_DONATE
                //PAGE_ID_ABOUT_ANILIB,
                //PAGE_ID_RULES
        )
    }

    fun getPage(pageId: String): Single<PageLibria> {
        val args: Map<String, String> = emptyMap()
        return client.get("${apiConfig.baseUrl}/$pageId", args)
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