package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.FavoriteApi
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig,
    private val authRepository: AuthRepository,
) {

    private data class FavoriteKey(val userId: Int, val releaseId: ReleaseId)
    private val favoriteChanges = MutableStateFlow<Map<FavoriteKey, Boolean>>(emptyMap())

    // Successful local actions take precedence over older list/full-release snapshots.
    fun observeFavoriteChanges(releaseId: ReleaseId): Flow<Boolean?> = combine(
        authRepository.observeUser(), favoriteChanges,
    ) { user, changes ->
        user?.let { changes[FavoriteKey(it.id, releaseId)] }
    }.distinctUntilChanged()

    suspend fun getFavorites(page: Int): Paginated<Release> = withContext(Dispatchers.IO) {
        favoriteApi
            .getFavorites(page)
            .toDomain { it.toDomain(apiUtils, apiConfig) }
            .also { updateMiddleware.handle(it.data) }
    }

    suspend fun deleteFavorite(releaseId: ReleaseId): Release = withContext(Dispatchers.IO) {
        val userId = authRepository.getUser()?.id
        favoriteApi
            .deleteFavorite(releaseId.id)
            .toDomain(apiUtils, apiConfig)
            .also { rememberFavorite(userId, releaseId, false) }
    }

    suspend fun addFavorite(releaseId: ReleaseId): Release = withContext(Dispatchers.IO) {
        val userId = authRepository.getUser()?.id
        favoriteApi
            .addFavorite(releaseId.id)
            .toDomain(apiUtils, apiConfig)
            .also { rememberFavorite(userId, releaseId, true) }
    }

    private fun rememberFavorite(userId: Int?, releaseId: ReleaseId, isFavorite: Boolean) {
        userId ?: return
        favoriteChanges.update { it + (FavoriteKey(userId, releaseId) to isFavorite) }
    }
}
