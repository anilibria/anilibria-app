package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.FavoritesApiDataSource
import ru.radiationx.data.apinext.models.filters.FavoritesFilterData
import ru.radiationx.data.apinext.models.filters.FavoritesFilterForm
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoritesApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
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

    suspend fun deleteRelease(releaseId: ReleaseId): Unit = withContext(Dispatchers.IO) {
        favoriteApi.deleteRelease(releaseId)
    }

    suspend fun addRelease(releaseId: ReleaseId): Unit = withContext(Dispatchers.IO) {
        favoriteApi.addRelease(releaseId)
    }
}