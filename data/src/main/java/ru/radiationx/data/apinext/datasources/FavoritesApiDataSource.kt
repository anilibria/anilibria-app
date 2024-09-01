package ru.radiationx.data.apinext.datasources

import anilibria.api.favorites.FavoritesApi
import anilibria.api.shared.ReleaseIdNetwork
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.apinext.models.filters.FavoritesFilterData
import ru.radiationx.data.apinext.models.filters.FavoritesFilterForm
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toDomainFilterYear
import ru.radiationx.data.apinext.toListQuery
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId

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
        return api
            .getReleases(
                page = page,
                limit = null,
                years = form?.years?.toListQuery(),
                types = form?.types?.toListQuery(),
                genres = form?.genres?.toListQuery(),
                search = form?.query,
                sorting = form?.sorting?.value,
                ageRatings = form?.ageRatings?.toListQuery()
            )
            .toDomain { it.toDomain() }
    }

    suspend fun getReleaseIds(): Set<ReleaseId> {
        return api.getIds().map { ReleaseId(it) }.toSet()
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        api.deleteReleases(listOf(ReleaseIdNetwork(releaseId.id)))
    }

    suspend fun addRelease(releaseId: ReleaseId) {
        api.addReleases(listOf(ReleaseIdNetwork(releaseId.id)))
    }
}