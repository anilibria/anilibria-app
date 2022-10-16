package ru.radiationx.data.repository

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.datasource.remote.api.SearchApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.*
import ru.radiationx.data.entity.app.search.SearchForm
import ru.radiationx.data.entity.app.search.SuggestionItem
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val searchApi: SearchApi,
    private val genresHolder: GenresHolder,
    private val yearsHolder: YearsHolder,
    private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun observeGenres(): Observable<MutableList<GenreItem>> = genresHolder
        .observeGenres()
        .observeOn(schedulers.ui())

    fun observeYears(): Observable<MutableList<YearItem>> = yearsHolder
        .observeYears()
        .observeOn(schedulers.ui())

    fun fastSearch(query: String): Single<List<SuggestionItem>> = searchApi
        .fastSearch(query)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun searchReleases(form: SearchForm, page: Int): Single<Paginated<List<ReleaseItem>>> {
        val yearsQuery = form.years?.joinToString(",") { it.value }.orEmpty()
        val seasonsQuery = form.seasons?.joinToString(",") { it.value }.orEmpty()
        val genresQuery = form.genres?.joinToString(",") { it.value }.orEmpty()
        val sortStr = when (form.sort) {
            SearchForm.Sort.RATING -> "2"
            SearchForm.Sort.DATE -> "1"
        }
        val onlyCompletedStr = if (form.onlyCompleted) "2" else "1"

        return searchReleases(genresQuery, yearsQuery, seasonsQuery, sortStr, onlyCompletedStr, page)
    }

    fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        onlyCompleted: String,
        page: Int
    ): Single<Paginated<List<ReleaseItem>>> = searchApi
        .searchReleases(genre, year, season, sort, onlyCompleted, page)
        .doOnSuccess {
            val newItems = mutableListOf<ReleaseItem>()
            val updItems = mutableListOf<ReleaseUpdate>()
            it.data.forEach { item ->
                val updItem = releaseUpdateHolder.getRelease(item.id)
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

    fun getSeasons(): Single<List<SeasonItem>> = Single
        .just(listOf("зима", "весна", "лето", "осень").map { SeasonItem(it.capitalize(), it) })

}
