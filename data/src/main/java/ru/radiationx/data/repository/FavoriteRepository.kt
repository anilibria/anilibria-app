package ru.radiationx.data.repository

import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.FavoriteApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoriteApi
) {

    suspend fun getFavorites(page: Int): Paginated<List<ReleaseItem>> = favoriteApi
        .getFavorites(page)

    suspend fun deleteFavorite(releaseId: Int): ReleaseItem = favoriteApi
        .deleteFavorite(releaseId)

    suspend fun addFavorite(releaseId: Int): ReleaseItem = favoriteApi
        .addFavorite(releaseId)
}