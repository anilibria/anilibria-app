package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
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

    fun observeGenres(): Flow<List<GenreItem>> = genresHolder
        .observeGenres()

    fun observeYears(): Flow<List<YearItem>> = yearsHolder
        .observeYears()

    suspend fun fastSearch(query: String): List<SuggestionItem> = searchApi
        .fastSearch(query)

    suspend fun searchReleases(form: SearchForm, page: Int): Paginated<List<ReleaseItem>> {
        val yearsQuery = form.years?.joinToString(",") { it.value }.orEmpty()
        val seasonsQuery = form.seasons?.joinToString(",") { it.value }.orEmpty()
        val genresQuery = form.genres?.joinToString(",") { it.value }.orEmpty()
        val sortStr = when (form.sort) {
            SearchForm.Sort.RATING -> "2"
            SearchForm.Sort.DATE -> "1"
        }
        val onlyCompletedStr = if (form.onlyCompleted) "2" else "1"

        return searchReleases(
            genresQuery,
            yearsQuery,
            seasonsQuery,
            sortStr,
            onlyCompletedStr,
            page
        )
    }

    suspend fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        onlyCompleted: String,
        page: Int
    ): Paginated<List<ReleaseItem>> = searchApi
        .searchReleases(genre, year, season, sort, onlyCompleted, page)
        .also {
            val newItems = mutableListOf<ReleaseItem>()
            val updItems = mutableListOf<ReleaseUpdate>()
            it.data.forEach { item ->
                val updItem = releaseUpdateHolder.getRelease(item.id)
                if (updItem == null) {
                    newItems.add(item)
                } else {

                    item.isNew =
                        item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                    /*if (item.torrentUpdate > updItem.timestamp) {
                        updItem.timestamp = item.torrentUpdate
                        updItems.add(updItem)
                    }*/
                }
            }
            releaseUpdateHolder.putAllRelease(newItems)
            releaseUpdateHolder.updAllRelease(updItems)
        }

    suspend fun getGenres(): List<GenreItem> = searchApi
        .getGenres()
        .also {
            genresHolder.saveGenres(it)
        }

    suspend fun getYears(): List<YearItem> = searchApi
        .getYears()
        .also {
            yearsHolder.saveYears(it)
        }

    suspend fun getSeasons(): List<SeasonItem> {
        return listOf("зима", "весна", "лето", "осень").map { SeasonItem(it.capitalize(), it) }
    }

}
