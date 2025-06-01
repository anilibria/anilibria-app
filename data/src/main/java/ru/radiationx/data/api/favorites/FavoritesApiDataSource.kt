package ru.radiationx.data.api.favorites

import anilibria.api.favorites.FavoritesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.api.favorites.mapper.toRequest
import ru.radiationx.data.api.favorites.models.FavoritesFilterData
import ru.radiationx.data.api.favorites.models.FavoritesFilterForm
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.releases.mapper.toNetwork
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.toDomain
import ru.radiationx.data.api.shared.filter.toDomainFilterYear
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.common.ReleaseId
import toothpick.InjectConstructor

@InjectConstructor
class FavoritesApiDataSource(
    private val api: FavoritesApi
) {

    suspend fun getFilterData(): FavoritesFilterData {
        return coroutineScope {
            val ageRatings = async { api.getAgeRatings() }
            val genres = async { api.getGenres() }
            val types = async { api.getTypes() }
            val sortings = async { api.getSortings() }
            val years = async { api.getYears() }

            FavoritesFilterData(
                ageRatings = ageRatings.await().map { it.toDomain() },
                genres = genres.await().map { it.toDomain() },
                types = types.await().map { it.toDomain() },
                sortings = sortings.await().map { it.toDomain() },
                years = years.await().map { it.toDomainFilterYear() }
            )
        }
    }

    suspend fun getReleases(
        page: Int,
        form: FavoritesFilterForm?
    ): Paginated<Release> {
        val request = form.toRequest(page)
        return api
            .getReleases(request)
            .toDomain { it.toDomain() }
    }

    suspend fun getReleaseIds(): Set<ReleaseId> {
        return api.getIds().map { ReleaseId(it) }.toSet()
    }

    suspend fun deleteRelease(releaseId: ReleaseId): Set<ReleaseId> {
        return api.deleteReleases(listOf(releaseId.toNetwork())).map { ReleaseId(it) }.toSet()
    }

    suspend fun addRelease(releaseId: ReleaseId): Set<ReleaseId> {
        return api.addReleases(listOf(releaseId.toNetwork())).map { ReleaseId(it) }.toSet()
    }
}