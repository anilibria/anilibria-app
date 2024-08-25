package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.radiationx.data.apinext.models.CollectionReleaseId
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.CollectionsRepository
import toothpick.InjectConstructor

@InjectConstructor
class CollectionsInteractor(
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
        collectionsRepository.deleteRelease(releaseId)
        releaseIds.update {
            it.removeById(releaseId)
        }
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType) {
        collectionsRepository.addRelease(releaseId, type)
        releaseIds.update {
            it.removeById(releaseId).plus(CollectionReleaseId(releaseId, type))
        }
    }

    private fun Set<CollectionReleaseId>.removeById(releaseId: ReleaseId): Set<CollectionReleaseId> {
        return filterNotTo(LinkedHashSet()) { it.id == releaseId }
    }
}