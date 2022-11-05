package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
    private val historyStorage: HistoryHolder,
    private val updateHolder: ReleaseUpdateHolder
) {

    suspend fun getReleases(): List<ReleaseItem> = historyStorage
        .getEpisodes()
        .asReversed()

    fun observeReleases(): Flow<List<ReleaseItem>> = historyStorage
        .observeEpisodes()
        .map { it.asReversed() }

    fun putRelease(releaseItem: ReleaseItem) {
        historyStorage.putRelease(releaseItem)
        updateHolder.viewRelease(releaseItem)
    }

    fun removeRelease(id: Int) = historyStorage.removerRelease(id)
}