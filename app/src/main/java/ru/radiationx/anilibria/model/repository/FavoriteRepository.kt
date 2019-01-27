package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.api.FavoriteApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

class FavoriteRepository(
        private val schedulers: SchedulersProvider,
        private val favoriteApi: FavoriteApi
) {

    fun getFavorites(): Single<Paginated<List<ReleaseItem>>> = favoriteApi
            .getFavorites()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteFavorite(releaseId: Int): Single<ReleaseItem> = favoriteApi
            .deleteFavorite(releaseId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendFav(releaseId: Int): Single<ReleaseItem> = favoriteApi
            .addFavorite(releaseId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}