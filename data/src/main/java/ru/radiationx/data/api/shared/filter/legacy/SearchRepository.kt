package ru.radiationx.data.api.shared.filter.legacy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val updateMiddleware: ReleaseUpdateMiddleware,
) {


    fun observeGenres(): Flow<List<GenreItem>> = flowOf(emptyList())

    fun observeYears(): Flow<List<YearItem>> = flowOf(emptyList())


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
