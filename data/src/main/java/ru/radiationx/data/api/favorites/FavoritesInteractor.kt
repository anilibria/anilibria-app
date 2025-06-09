package ru.radiationx.data.api.favorites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.network.SharedRequests
import javax.inject.Inject

class FavoritesInteractor @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {

    private val sharedRequest = SharedRequests<Unit, Set<ReleaseId>>()

    private val releaseIds = MutableStateFlow<Set<ReleaseId>>(emptySet())

    fun observeIds(): Flow<Set<ReleaseId>> = releaseIds

    suspend fun loadReleaseIds(): Set<ReleaseId> {
        return sharedRequest.request(Unit) {
            val ids = favoriteRepository.getReleaseIds()
            releaseIds.value = ids
            ids
        }
    }

    suspend fun toggle(releaseId: ReleaseId) {
        if (releaseIds.value.contains(releaseId)) {
            deleteRelease(releaseId)
        } else {
            addRelease(releaseId)
        }
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        val ids = favoriteRepository.deleteRelease(releaseId)
        releaseIds.value = ids
    }

    suspend fun addRelease(releaseId: ReleaseId) {
        val ids = favoriteRepository.addRelease(releaseId)
        releaseIds.value = ids
    }
}