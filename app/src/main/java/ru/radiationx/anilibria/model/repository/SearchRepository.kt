package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.api.SearchApi

class SearchRepository(private val searchApi: SearchApi) {

    fun fastSearch(query: String): Single<List<SearchItem>> = Single.defer {
        searchApi.fastSearch(query)
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<List<ReleaseItem>>> = Single.defer {
        searchApi.searchReleases(name, genre, page)
    }

}
