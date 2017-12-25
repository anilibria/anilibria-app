package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository(private val schedulers: SchedulersProvider,
                        private val releaseApi: ReleaseApi) {

    fun getRelease(releaseId: Int): Observable<ReleaseFull>
            = releaseApi.getRelease(releaseId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getGenres(): Observable<List<GenreItem>>
            = releaseApi.getGenres()
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getReleases(page: Int): Observable<Paginated<List<ReleaseItem>>>
            = releaseApi.getReleases(page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
