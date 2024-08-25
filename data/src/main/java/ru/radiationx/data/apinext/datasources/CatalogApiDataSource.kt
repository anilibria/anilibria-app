package ru.radiationx.data.apinext.datasources

import anilibria.api.catalog.CatalogApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.apinext.models.filters.CatalogFilterData
import ru.radiationx.data.apinext.models.filters.CatalogFilterForm
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toDomainFilterYear
import ru.radiationx.data.apinext.toListQuery
import ru.radiationx.data.apinext.toQuery
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import toothpick.InjectConstructor

@InjectConstructor
class CatalogApiDataSource(
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
        return api
            .getReleases(
                page = page,
                limit = null,
                genres = form?.genres?.toListQuery(),
                types = form?.types?.toListQuery(),
                seasons = form?.seasons?.toListQuery(),
                fromYear = form?.yearsRange?.first?.toQuery(),
                toYear = form?.yearsRange?.second?.toQuery(),
                search = form?.query,
                sorting = form?.sorting?.toQuery(),
                ageRatings = form?.ageRatings?.toListQuery(),
                publishStatuses = form?.publishStatuses?.toListQuery(),
                productionStatuses = form?.productionStatuses?.toListQuery()
            )
            .toDomain { it.toDomain() }
    }
}