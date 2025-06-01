package ru.radiationx.data.api.favorites

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.favorites.models.FavoritesFilterData
import ru.radiationx.data.api.favorites.models.FavoritesFilterForm
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import ru.radiationx.data.common.ReleaseId
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoritesApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    suspend fun getFilterData(): FavoritesFilterData = withContext(Dispatchers.IO) {
        favoriteApi.getFilterData()
    }

    suspend fun getReleases(
        page: Int,
        form: FavoritesFilterForm?
    ): Paginated<Release> = withContext(Dispatchers.IO) {
        favoriteApi
            .getReleases(page, form)
            .also { updateMiddleware.handle(it.data) }
    }

    suspend fun getReleaseIds(): Set<ReleaseId> = withContext(Dispatchers.IO) {
        favoriteApi.getReleaseIds()
    }

    suspend fun deleteRelease(releaseId: ReleaseId): Set<ReleaseId> = withContext(Dispatchers.IO) {
        favoriteApi.deleteRelease(releaseId)
    }

    suspend fun addRelease(releaseId: ReleaseId): Set<ReleaseId> = withContext(Dispatchers.IO) {
        favoriteApi.addRelease(releaseId)
    }
}