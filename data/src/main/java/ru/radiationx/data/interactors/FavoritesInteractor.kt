package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.FavoriteRepository

class FavoritesInteractor(
    private val favoriteRepository: FavoriteRepository
) {

    private val sharedRequest = SharedRequests<Unit, Set<ReleaseId>>()

    private val releaseIds = MutableStateFlow<Set<ReleaseId>>(emptySet())

    fun observeIds(): Flow<Set<ReleaseId>> = releaseIds

    suspend fun loadReleaseIds(): Set<ReleaseId> {
        return sharedRequest.request(Unit) {
            favoriteRepository.getReleaseIds()
        }
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        favoriteRepository.deleteRelease(releaseId)
        releaseIds.update { it.minus(releaseId) }
    }

    suspend fun addRelease(releaseId: ReleaseId) {
        favoriteRepository.addRelease(releaseId)
        releaseIds.update { it.plus(releaseId) }
    }
}