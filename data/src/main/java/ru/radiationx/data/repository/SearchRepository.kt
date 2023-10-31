package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.datasource.remote.api.SearchApi
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.mapper.toGenreItem
import ru.radiationx.data.entity.mapper.toSuggestionDomain
import ru.radiationx.data.entity.mapper.toYearItem
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.shared.ktx.capitalizeDefault
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val releaseApi: ReleaseApi,
    private val genresHolder: GenresHolder,
    private val yearsHolder: YearsHolder,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig,
) {

    private val searchIdRegex = Regex("^id(\\d{3,})\$")

    fun observeGenres(): Flow<List<GenreItem>> = genresHolder
        .observeGenres()
        .flowOn(Dispatchers.IO)

    fun observeYears(): Flow<List<YearItem>> = yearsHolder
        .observeYears()
        .flowOn(Dispatchers.IO)

    private fun getQueryId(query: String): Int? {
        return searchIdRegex.find(query)?.let { matchResult ->
            matchResult.groupValues.getOrNull(1)?.toIntOrNull()
        }
    }

    suspend fun fastSearch(query: String): List<SuggestionItem> = withContext(Dispatchers.IO) {
        val releaseId = getQueryId(query)
        if (releaseId != null) {
            releaseApi
                .getReleasesByIds(listOf(releaseId))
                .map { it.toSuggestionDomain(apiUtils, apiConfig) }
        } else {
            searchApi
                .fastSearch(query)
                .map { it.toDomain(apiUtils, apiConfig) }
        }
    }

    suspend fun searchReleases(form: SearchForm, page: Int): Paginated<Release> {
        val yearsQuery = form.years.joinToString(",") { it.value }
        val seasonsQuery = form.seasons.joinToString(",") { it.value }
        val genresQuery = form.genres.joinToString(",") { it.value }
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

    private suspend fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        onlyCompleted: String,
        page: Int,
    ): Paginated<Release> = withContext(Dispatchers.IO) {
        searchApi
            .searchReleases(genre, year, season, sort, onlyCompleted, page)
            .toDomain { it.toDomain(apiUtils, apiConfig) }
            .also { updateMiddleware.handle(it.data) }
    }

    suspend fun getGenres(): List<GenreItem> = withContext(Dispatchers.IO) {
        searchApi
            .getGenres()
            .map { it.toGenreItem() }
            .also {
                genresHolder.saveGenres(it)
            }
    }

    suspend fun getYears(): List<YearItem> = withContext(Dispatchers.IO) {
        searchApi
            .getYears()
            .map { it.toYearItem() }
            .also {
                yearsHolder.saveYears(it)
            }
    }

    suspend fun getSeasons(): List<SeasonItem> {
        return withContext(Dispatchers.IO) {
            listOf("зима", "весна", "лето", "осень").map { SeasonItem(it.capitalizeDefault(), it) }
        }
    }

}
