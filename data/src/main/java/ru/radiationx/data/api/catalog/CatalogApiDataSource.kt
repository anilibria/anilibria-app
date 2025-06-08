package ru.radiationx.data.api.catalog

import anilibria.api.catalog.CatalogApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.api.catalog.mapper.toRequest
import ru.radiationx.data.api.catalog.models.CatalogFilterData
import ru.radiationx.data.api.catalog.models.CatalogFilterForm
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.toDomain
import ru.radiationx.data.api.shared.filter.toDomainFilterYear
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import javax.inject.Inject

class CatalogApiDataSource @Inject constructor(
    private val api: CatalogApi
) {

    suspend fun getFilterData(): CatalogFilterData {
        return coroutineScope {
            val ageRatings = async { api.getAgeRatings() }
            val genres = async { api.getGenres() }
            val productionStatuses = async { api.getProductionStatuses() }
            val publishStatuses = async { api.getPublishStatuses() }
            val types = async { api.getTypes() }
            val seasons = async { api.getSeasons() }
            val sortings = async { api.getSortings() }
            val years = async { api.getYears() }

            CatalogFilterData(
                ageRatings = ageRatings.await().map { it.toDomain() },
                genres = genres.await().map { it.toDomain() },
                productionStatuses = productionStatuses.await().map { it.toDomain() },
                publishStatuses = publishStatuses.await().map { it.toDomain() },
                types = types.await().map { it.toDomain() },
                seasons = seasons.await().map { it.toDomain() },
                sortings = sortings.await().map { it.toDomain() },
                years = years.await().map { it.toDomainFilterYear() }
            )
        }
    }

    suspend fun getReleases(
        page: Int,
        form: CatalogFilterForm?
    ): Paginated<Release> {
        val request = form.toRequest(page)
        return api
            .getReleases(request)
            .toDomain { it.toDomain() }
    }
}