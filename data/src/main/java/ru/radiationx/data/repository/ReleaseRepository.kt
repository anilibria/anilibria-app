package ru.radiationx.data.repository

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
    private val releaseApi: ReleaseApi,
    private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    suspend fun getRandomRelease(): RandomRelease = releaseApi.getRandomRelease()

    suspend fun getRelease(releaseId: Int): ReleaseFull = releaseApi
        .getRelease(releaseId)
        .also(this::fillReleaseUpdate)

    suspend fun getRelease(releaseIdName: String): ReleaseFull = releaseApi
        .getRelease(releaseIdName)
        .also(this::fillReleaseUpdate)

    suspend fun getReleasesById(ids: List<Int>): List<ReleaseItem> = releaseApi
        .getReleasesByIds(ids)
        .also { fillReleasesUpdate(it) }

    suspend fun getReleases(page: Int): Paginated<List<ReleaseItem>> = releaseApi
        .getReleases(page)
        .also { fillReleasesUpdate(it.data) }

    private fun fillReleasesUpdate(items: List<ReleaseItem>) {
        val newItems = mutableListOf<ReleaseItem>()
        val updItems = mutableListOf<ReleaseUpdate>()
        items.forEach { item ->
            val updItem = releaseUpdateHolder.getRelease(item.id)
            if (updItem == null) {
                newItems.add(item)
            } else {
                item.isNew =
                    item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
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
            val newUpdItem = updItem.copy(
                timestamp = item.torrentUpdate,
                lastOpenTimestamp = updItem.timestamp
            )
            releaseUpdateHolder.updRelease(newUpdItem)
        }
    }
}
