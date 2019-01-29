package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.PagesParser

/**
 * Created by radiationx on 13.01.18.
 */
class PageApi(
        private val client: IClient,
        private val pagesParser: PagesParser
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
        return client.get("${Api.BASE_URL}/$pageId", args)
                .map { pagesParser.baseParse(it) }
    }
}