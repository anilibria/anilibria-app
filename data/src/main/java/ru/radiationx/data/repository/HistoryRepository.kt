package ru.radiationx.data.repository

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val historyStorage: HistoryHolder
) {

    fun getReleases():Single<List<ReleaseItem>> = historyStorage
        .getEpisodes()
        .map { it.asReversed() }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun observeReleases(): Observable<MutableList<ReleaseItem>> = historyStorage
            .observeEpisodes()
            .map { it.asReversed() }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun putRelease(releaseItem: ReleaseItem) = historyStorage.putRelease(releaseItem)

    fun removeRelease(id: Int) = historyStorage.removerRelease(id)
}