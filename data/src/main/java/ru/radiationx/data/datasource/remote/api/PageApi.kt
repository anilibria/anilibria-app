package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.parsers.PagesParser
import ru.radiationx.data.entity.domain.page.PageLibria
import ru.radiationx.data.entity.response.page.VkCommentsResponse
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val pagesParser: PagesParser,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {
    companion object {
        const val PAGE_PATH_TEAM = "pages/team.php"
        const val PAGE_PATH_DONATE = "pages/donate.php"

        val PAGE_IDS = listOf(
            PAGE_PATH_TEAM,
            PAGE_PATH_DONATE
        )
    }

    suspend fun getPage(pagePath: String): PageLibria {
        val args: Map<String, String> = emptyMap()
        return client
            .get("${apiConfig.baseUrl}/$pagePath", args)
            .let { pagesParser.baseParse(it) }
    }

    suspend fun getComments(): VkCommentsResponse {
        val args: Map<String, String> = mapOf(
            "query" to "vkcomments"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }
}