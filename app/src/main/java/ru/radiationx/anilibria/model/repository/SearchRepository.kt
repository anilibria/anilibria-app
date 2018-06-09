package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.SearchApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

class SearchRepository(
        private val schedulers: SchedulersProvider,
        private val searchApi: SearchApi,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun fastSearch(query: String): Observable<List<SearchItem>> = searchApi
            .fastSearch(query)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun searchReleases(name: String, genre: String, page: Int): Observable<Paginated<List<ReleaseItem>>> = searchApi
            .searchReleases(name, genre, page)
            .doOnSuccess {
                val newItems = mutableListOf<ReleaseItem>()
                it.data.forEach { item ->
                    val updItem = releaseUpdateHolder.getRelease(item.id)
                    if (updItem == null) {
                        newItems.add(item)
                    } else {
                        item.isNew = item.torrentUpdate > updItem.timestamp
                    }
                }
                releaseUpdateHolder.putAllRelease(newItems)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
