package ru.radiationx.data.repository

import anilibria.api.favorites.FavoritesApi
import anilibria.api.shared.ReleaseIdNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.models.filters.FavoritesFilterData
import ru.radiationx.data.apinext.models.filters.FavoritesFilterForm
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toDomainFilterYear
import ru.radiationx.data.apinext.toQuery
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val favoriteApi: FavoritesApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    suspend fun getFilterData(): FavoritesFilterData = withContext(Dispatchers.IO) {
        val ageRatings = async { favoriteApi.getAgeRatings() }
        val genres = async { favoriteApi.getGenres() }
        val types = async { favoriteApi.getTypes() }
        val sortings = async { favoriteApi.getSorting() }
        val years = async { favoriteApi.getYears() }

        FavoritesFilterData(
            ageRatings = ageRatings.await().map { it.toDomain() },
            genres = genres.await().map { it.toDomain() },
            types = types.await().map { it.toDomain() },
            sortings = sortings.await().map { it.toDomain() },
            years = years.await().map { it.toDomainFilterYear() }
        )
    }

    suspend fun getFavorites(
        page: Int,
        form: FavoritesFilterForm
    ): Paginated<Release> = withContext(Dispatchers.IO) {
        favoriteApi
            .getReleases(
                page = page,
                limit = null,
                years = form.years?.toQuery(),
                types = form.types?.toQuery(),
                genres = form.genres?.toQuery(),
                search = form.query,
                sorting = form.sorting?.value,
                ageRatings = form.ageRatings?.toQuery()

            )
            .toDomain { it.toDomain() }
            .also { updateMiddleware.handle(it.data) }
    }

    suspend fun getFavoritesIds(): List<ReleaseId> = withContext(Dispatchers.IO) {
        favoriteApi.getIds().map { ReleaseId(it) }
    }

    suspend fun deleteFavorite(releaseId: ReleaseId): Unit = withContext(Dispatchers.IO) {
        favoriteApi.deleteReleases(listOf(ReleaseIdNetwork(releaseId.id)))
    }

    suspend fun addFavorite(releaseId: ReleaseId): Unit = withContext(Dispatchers.IO) {
        favoriteApi.addReleases(listOf(ReleaseIdNetwork(releaseId.id)))
    }
}