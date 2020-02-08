package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.datasource.remote.api.FavoriteApi
import ru.radiationx.data.SchedulersProvider
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val favoriteApi: FavoriteApi
) {

    fun getFavorites(page: Int): Single<Paginated<List<ReleaseItem>>> = favoriteApi
            .getFavorites(page)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteFavorite(releaseId: Int): Single<ReleaseItem> = favoriteApi
            .deleteFavorite(releaseId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun addFavorite(releaseId: Int): Single<ReleaseItem> = favoriteApi
            .addFavorite(releaseId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}