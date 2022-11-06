package ru.radiationx.data.repository

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
    private val apiConfig: ApiConfig
) {

    suspend fun getFavorites(page: Int): Paginated<Release> = favoriteApi
        .getFavorites(page)
        .toDomain { it.toDomain(apiUtils, apiConfig) }
        .also { updateMiddleware.handle(it.data) }

    suspend fun deleteFavorite(releaseId: ReleaseId): Release = favoriteApi
        .deleteFavorite(releaseId.id)
        .toDomain(apiUtils, apiConfig)

    suspend fun addFavorite(releaseId: ReleaseId): Release = favoriteApi
        .addFavorite(releaseId.id)
        .toDomain(apiUtils, apiConfig)
}