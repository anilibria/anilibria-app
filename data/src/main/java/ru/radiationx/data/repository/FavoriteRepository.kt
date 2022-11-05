package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.api.FavoriteApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    suspend fun getFavorites(page: Int): Paginated<List<Release>> = favoriteApi
        .getFavorites(page)
        .also { updateMiddleware.handle(it.data) }

    suspend fun deleteFavorite(releaseId: Int): Release = favoriteApi
        .deleteFavorite(releaseId)

    suspend fun addFavorite(releaseId: Int): Release = favoriteApi
        .addFavorite(releaseId)
}