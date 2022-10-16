package ru.radiationx.data.repository

import android.util.Log
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.ReleaseUpdate
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val releaseApi: ReleaseApi,
    private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun getRandomRelease(): Single<RandomRelease> = releaseApi
        .getRandomRelease()
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun getRelease(releaseId: Int): Single<ReleaseFull> = releaseApi
        .getRelease(releaseId)
        .doOnSuccess(this::fillReleaseUpdate)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun getRelease(releaseIdName: String): Single<ReleaseFull> = releaseApi
        .getRelease(releaseIdName)
        .doOnSuccess(this::fillReleaseUpdate)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun getReleasesById(ids: List<Int>): Single<List<ReleaseItem>> = releaseApi
        .getReleasesByIds(ids)
        .doOnSuccess { fillReleasesUpdate(it) }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> = releaseApi
        .getReleases(page)
        .doOnSuccess { fillReleasesUpdate(it.data) }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    private fun fillReleasesUpdate(items: List<ReleaseItem>) {
        val newItems = mutableListOf<ReleaseItem>()
        val updItems = mutableListOf<ReleaseUpdate>()
        items.forEach { item ->
            val updItem = releaseUpdateHolder.getRelease(item.id)
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

    private fun fillReleaseUpdate(item: ReleaseItem) {
        val updItem = releaseUpdateHolder.getRelease(item.id)

        if (updItem == null) {
            releaseUpdateHolder.putRelease(item)
        } else {
            item.isNew = item.torrentUpdate > updItem.lastOpenTimestamp
            updItem.timestamp = item.torrentUpdate
            updItem.lastOpenTimestamp = updItem.timestamp
            releaseUpdateHolder.updRelease(updItem)
        }
    }
}
