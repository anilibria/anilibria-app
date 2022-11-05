package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.datasource.remote.api.SearchApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SearchForm
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val genresHolder: GenresHolder,
    private val yearsHolder: YearsHolder,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    fun observeGenres(): Flow<List<GenreItem>> = genresHolder
        .observeGenres()

    fun observeYears(): Flow<List<YearItem>> = yearsHolder
        .observeYears()

    suspend fun fastSearch(query: String): List<SuggestionItem> = searchApi
        .fastSearch(query)

    suspend fun searchReleases(form: SearchForm, page: Int): Paginated<Release> {
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
    ): Paginated<Release> = searchApi
        .searchReleases(genre, year, season, sort, onlyCompleted, page)
        .also { updateMiddleware.handle(it.data) }

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
