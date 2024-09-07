package ru.radiationx.data.apinext.datasources

import anilibria.api.collections.CollectionsApi
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.ReleaseIdNetwork
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.apinext.models.CollectionReleaseId
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.apinext.models.filters.CollectionsFilterData
import ru.radiationx.data.apinext.models.filters.CollectionsFilterForm
import ru.radiationx.data.apinext.toCollectionType
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toDomainFilterYear
import ru.radiationx.data.apinext.toListQuery
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import toothpick.InjectConstructor

@InjectConstructor
class CollectionsApiDataSource(
    private val api: CollectionsApi
) {

    suspend fun getFilterData(): CollectionsFilterData {
        return coroutineScope {
            val ageRatings = async { api.getAgeRatings() }
            val genres = async { api.getGenres() }
            val types = async { api.getTypes() }
            val years = async { api.getYears() }

            CollectionsFilterData(
                ageRatings = ageRatings.await().map { it.toDomain() },
                genres = genres.await().map { it.toDomain() },
                types = types.await().map { it.toDomain() },
                years = years.await().map { it.toDomainFilterYear() }
            )
        }
    }

    suspend fun getReleases(
        type: CollectionType,
        page: Int,
        form: CollectionsFilterForm?
    ): Paginated<Release> {
        return api
            .getReleases(
                collectionType = type.toListQuery(),
                page = page,
                limit = null,
                years = form?.years?.toListQuery(),
                types = form?.types?.toListQuery(),
                genres = form?.genres?.toListQuery(),
                search = form?.query,
                ageRatings = form?.ageRatings?.toListQuery()
            )
            .toDomain { it.toDomain() }
    }

    suspend fun getReleaseIds(): Set<CollectionReleaseId> {
        return api.getIds().map {
            CollectionReleaseId(
                id = ReleaseId(it.releaseId),
                type = it.typeOfCollection.toCollectionType()
            )
        }.toSet()
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        api.deleteReleases(listOf(ReleaseIdNetwork(releaseId.id)))
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType) {
        api.addReleases(listOf(CollectionReleaseIdNetwork(releaseId.id, type.toListQuery())))
    }
}