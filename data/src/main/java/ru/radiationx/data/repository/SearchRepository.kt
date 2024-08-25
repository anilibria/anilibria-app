package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val genresHolder: GenresHolder,
    private val yearsHolder: YearsHolder,
    private val updateMiddleware: ReleaseUpdateMiddleware,
) {


    fun observeGenres(): Flow<List<GenreItem>> = genresHolder
        .observeGenres()
        .flowOn(Dispatchers.IO)

    fun observeYears(): Flow<List<YearItem>> = yearsHolder
        .observeYears()
        .flowOn(Dispatchers.IO)


    //todo API2 update usage
    /*suspend fun fastSearch(query: String): Suggestions = withContext(Dispatchers.IO) {
        val releaseId = getQueryId(query)
        val items = if (releaseId != null) {
            releaseApi
                .getReleasesByIds(listOf(releaseId))
                .map { it.toSuggestionDomain(apiUtils, apiConfig) }
        } else {
            searchApi
                .fastSearch(query)
                .map { it.toDomain(apiUtils, apiConfig) }
        }
        Suggestions(query, items)
    }*/

    suspend fun searchReleases(form: SearchForm, page: Int): Paginated<Release> {
        TODO("delete")
    }

    // todo API2 delete
    private suspend fun searchReleases(
        genre: String,
        year: String,
        season: String,
        sort: String,
        onlyCompleted: String,
        page: Int,
    ): Paginated<Release> = withContext(Dispatchers.IO) {
        TODO("delete")
    }

    suspend fun getGenres(): List<GenreItem> = withContext(Dispatchers.IO) {
        TODO("delete")
    }

    suspend fun getYears(): List<YearItem> = withContext(Dispatchers.IO) {
        TODO("delete")
    }

    suspend fun getSeasons(): List<SeasonItem> {
        TODO("delete")
    }

}
