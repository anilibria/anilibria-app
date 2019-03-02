package ru.radiationx.anilibria.model.repository

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.ReleaseUpdate
import ru.radiationx.anilibria.entity.app.release.YearItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.holders.GenresHolder
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.holders.YearsHolder
import ru.radiationx.anilibria.model.data.remote.api.SearchApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

class SearchRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val searchApi: SearchApi,
        private val genresHolder: GenresHolder,
        private val yearsHolder: YearsHolder,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun observeGenres(): Observable<MutableList<GenreItem>> = genresHolder.observeGenres()
    fun observeYears(): Observable<MutableList<YearItem>> = yearsHolder.observeYears()

    fun fastSearch(query: String): Single<List<SearchItem>> = searchApi
            .fastSearch(query)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun searchReleases(genre: String, year: String, sort: String, page: Int): Single<Paginated<List<ReleaseItem>>> = searchApi
            .searchReleases(genre, year, sort, page)
            .doOnSuccess {
                val newItems = mutableListOf<ReleaseItem>()
                val updItems = mutableListOf<ReleaseUpdate>()
                it.data.forEach { item ->
                    val updItem = releaseUpdateHolder.getRelease(item.id)
                    Log.e("lalalupdata", "${item.id}, ${item.torrentUpdate} : ${updItem?.id}, ${updItem?.timestamp}, ${updItem?.lastOpenTimestamp}")
                    if (updItem == null) {
                        newItems.add(item)
                    } else {

                        item.isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                        /*if (item.torrentUpdate > updItem.timestamp) {
                            updItem.timestamp = item.torrentUpdate
                            updItems.add(updItem)
                        }*/
                    }
                }
                releaseUpdateHolder.putAllRelease(newItems)
                releaseUpdateHolder.updAllRelease(updItems)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun getGenres(): Single<List<GenreItem>> = searchApi
            .getGenres()
            .doOnSuccess {
                genresHolder.saveGenres(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getYears(): Single<List<YearItem>> = searchApi
            .getYears()
            .doOnSuccess {
                yearsHolder.saveYears(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
