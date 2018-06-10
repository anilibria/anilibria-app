package ru.radiationx.anilibria.model.repository

import android.util.Log
import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.model.data.holders.GenresHolder
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository(
        private val schedulers: SchedulersProvider,
        private val releaseApi: ReleaseApi,
        private val genresHolder: GenresHolder,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun observeGenres(): Observable<MutableList<GenreItem>> = genresHolder.observeGenres()

    fun getRelease(releaseId: Int): Observable<ReleaseFull> = releaseApi
            .getRelease(releaseId)
            .doOnSuccess { item ->
                suka(item)


            }
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun suka(item: ReleaseItem) {
        val updItem = releaseUpdateHolder.getRelease(item.id)

        if (updItem == null) {
            releaseUpdateHolder.putRelease(item)
        } else {
            item.isNew = item.torrentUpdate > updItem.lastOpenTimestamp
            updItem.timestamp = item.torrentUpdate
            updItem.lastOpenTimestamp = updItem.timestamp
            Log.e("lalalupdata", "updRelease, ${updItem.id}, ${updItem.timestamp}, ${updItem.lastOpenTimestamp}")
            releaseUpdateHolder.updRelease(updItem)
        }
    }

    fun getRelease(releaseIdName: String): Observable<ReleaseFull> = releaseApi
            .getRelease(releaseIdName)
            .doOnSuccess { item ->
                suka(item)
            }
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
            .doOnSuccess {
                val newItems = mutableListOf<ReleaseItem>()
                val updItems = mutableListOf<ReleaseUpdate>()
                it.data.forEach { item ->
                    val updItem = releaseUpdateHolder.getRelease(item.id)
                    Log.e("lalalupdata", "${item.id}, ${item.torrentUpdate} : ${updItem?.id}, ${updItem?.timestamp}, ${updItem?.lastOpenTimestamp}")
                    if (updItem == null) {
                        newItems.add(item)
                    } else {

                        item.isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                        /*if (item.torrentUpdate > updItem.timestamp) {
                            updItem.timestamp = item.torrentUpdate
                            updItems.add(updItem)
                        }*/
                    }
                }
                releaseUpdateHolder.putAllRelease(newItems)
                releaseUpdateHolder.updAllRelease(updItems)
            }
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

    fun sendComment(code: String, id: Int, text: String, sessId: String): Observable<Paginated<List<Comment>>> = releaseApi
            .sendComment(code, id, text, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}
