package ru.radiationx.anilibria.data.repository

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.data.api.models.search.SearchItem
import ru.radiationx.anilibria.data.api.modules.SearchApi

class SearchRepository(private val searchApi: SearchApi) {

    fun fastSearch(query: String): Single<List<SearchItem>> = Single.defer {
        searchApi.fastSearch(query)
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<List<ReleaseItem>>> = Single.defer {
        searchApi.searchReleases(name, genre, page)
    }

}
