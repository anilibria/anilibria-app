package ru.radiationx.anilibria.data.api.modules

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.data.api.models.search.SearchItem
import ru.radiationx.anilibria.data.api.parsers.ReleaseParser
import ru.radiationx.anilibria.data.client.IClient

class SearchApi(private val client: IClient) {

    fun fastSearch(query: String): Single<List<SearchItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "ajax_call" to "y",
                "INPUT_ID" to "search-input-custom",
                "q" to query,
                "l" to "2"
        )
        return client.post(Api.BASE_URL, args)
                .map { ReleaseParser.fastSearch(it) }
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "search",
                "genre" to genre,
                "name" to name,
                "PAGEN_1" to page.toString()
        )
        return client.get(Api.API_URL, args)
                .map { ReleaseParser.releases(it) }
    }

}
