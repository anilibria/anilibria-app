package ru.radiationx.data.api.collections

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.api.collections.models.CollectionReleaseId
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.network.SharedRequests
import javax.inject.Inject

class CollectionsInteractor @Inject constructor(
    private val collectionsRepository: CollectionsRepository
) {

    private val sharedRequest = SharedRequests<Unit, Set<CollectionReleaseId>>()

    private val releaseIds = MutableStateFlow<Set<CollectionReleaseId>>(emptySet())

    fun observeIds(): Flow<Set<CollectionReleaseId>> = releaseIds

    fun observeIdsGrouped(): Flow<Map<CollectionType, List<ReleaseId>>> {
        return releaseIds.map { collectionIds ->
            collectionIds.groupBy(
                keySelector = { it.type },
                valueTransform = { it.id }
            )
        }
    }

    suspend fun loadReleaseIds(): Set<CollectionReleaseId> {
        return sharedRequest.request(Unit) {
            val ids = collectionsRepository.getReleaseIds()
            releaseIds.value = ids
            ids
        }
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        releaseIds.value = collectionsRepository.deleteRelease(releaseId)
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType) {
        releaseIds.value = collectionsRepository.addRelease(releaseId, type)
    }

    private fun Set<CollectionReleaseId>.removeById(releaseId: ReleaseId): Set<CollectionReleaseId> {
        return filterNotTo(LinkedHashSet()) { it.id == releaseId }
    }
}