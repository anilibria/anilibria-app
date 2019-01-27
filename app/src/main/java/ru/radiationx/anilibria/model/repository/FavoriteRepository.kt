package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.FavoriteData
import ru.radiationx.anilibria.model.data.holders.GenresHolder
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.CommentApi
import ru.radiationx.anilibria.model.data.remote.api.FavoriteApi
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

class FavoriteRepository(
        private val schedulers: SchedulersProvider,
        private val favoriteApi: FavoriteApi
) {

    fun getFavorites2(): Observable<FavoriteData> = favoriteApi
            .getFavorites2()
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteFavorite(id: Int, sessId: String): Observable<FavoriteData> = favoriteApi
            .deleteFavorite(id, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendFav(id: Int, isFaved: Boolean, sessId: String, sKey: String): Observable<Int> = favoriteApi
            .sendFav(id, isFaved, sessId, sKey)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}