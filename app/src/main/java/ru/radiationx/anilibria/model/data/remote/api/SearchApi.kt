package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import ru.radiationx.anilibria.model.data.remote.parsers.SearchParser

class SearchApi(
        private val client: IClient,
        private val releaseParser: ReleaseParser,
        private val searchParser: SearchParser
) {

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf("action" to "tags")
        return client.get(Api.API_URL, args)
                .map { searchParser.genres(it) }
    }

    fun fastSearch(query: String): Single<List<SearchItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "ajax_call" to "y",
                "INPUT_ID" to "search-input-custom",
                "q" to query,
                "l" to "2"
        )
        return client.post(Api.BASE_URL, args)
                .map { searchParser.fastSearch(it) }
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "search",
                "genres" to genre,
                "name" to name,
                "PAGEN_1" to page.toString()
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.releases(it) }
    }

}
