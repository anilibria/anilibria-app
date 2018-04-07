package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.model.data.holders.GenresHolder
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository(
        private val schedulers: SchedulersProvider,
        private val releaseApi: ReleaseApi,
        private val genresHolder: GenresHolder
) {

    fun observeGenres(): Observable<MutableList<GenreItem>> = genresHolder.observeGenres()

    fun getRelease(releaseId: Int): Observable<ReleaseFull> = releaseApi
            .getRelease(releaseId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getRelease(releaseIdName: String): Observable<ReleaseFull> = releaseApi
            .getRelease(releaseIdName)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getGenres(): Observable<List<GenreItem>> = releaseApi
            .getGenres()
            .map {
                val items = it.toMutableList()
                items.add(0, GenreItem().apply {
                    title = "Все"
                    value = ""
                })
                genresHolder.saveGenres(items)
                items.toList()
            }
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getReleases(page: Int): Observable<Paginated<List<ReleaseItem>>> = releaseApi
            .getReleases(page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getFavorites2(): Observable<FavoriteData> = releaseApi
            .getFavorites2()
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun deleteFavorite(id: Int, sessId: String): Observable<FavoriteData> = releaseApi
            .deleteFavorite(id, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getComments(id: Int, page: Int): Observable<Paginated<List<Comment>>> = releaseApi
            .getComments(id, page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendFav(id: Int, isFaved: Boolean, sessId: String, sKey: String): Observable<Int> = releaseApi
            .sendFav(id, isFaved, sessId, sKey)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}
