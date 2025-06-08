package ru.radiationx.data.api.collections

import anilibria.api.collections.CollectionsApi
import anilibria.api.shared.CollectionReleaseIdNetwork
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.api.collections.mapper.toDomain
import ru.radiationx.data.api.collections.mapper.toRequest
import ru.radiationx.data.api.collections.models.CollectionReleaseId
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.collections.models.CollectionsFilterData
import ru.radiationx.data.api.collections.models.CollectionsFilterForm
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.releases.mapper.toNetwork
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.toDomain
import ru.radiationx.data.api.shared.filter.toDomainFilterYear
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.common.ReleaseId
import javax.inject.Inject

class CollectionsApiDataSource @Inject constructor(
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
        val request = form.toRequest(type, page)
        return api
            .getReleases(request)
            .toDomain { it.toDomain() }
    }

    suspend fun getReleaseIds(): Set<CollectionReleaseId> {
        return api.getIds().toDomain()
    }

    suspend fun deleteRelease(releaseId: ReleaseId): Set<CollectionReleaseId> {
        val request = listOf(releaseId.toNetwork())
        return api.deleteReleases(request).toDomain()
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType): Set<CollectionReleaseId> {
        val request = listOf(CollectionReleaseIdNetwork(releaseId.id, type.toRequest()))
        return api.addReleases(request).toDomain()
    }

    private fun List<List<Any>>.toDomain(): Set<CollectionReleaseId> {
        return map {
            CollectionReleaseIdNetwork.ofList(it).toDomain()
        }.toSet()
    }
}