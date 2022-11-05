package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.release.Release
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
    private val historyStorage: HistoryHolder,
    private val updateHolder: ReleaseUpdateHolder
) {

    suspend fun getReleases(): List<Release> = historyStorage
        .getEpisodes()
        .asReversed()

    fun observeReleases(): Flow<List<Release>> = historyStorage
        .observeEpisodes()
        .map { it.asReversed() }

    fun putRelease(releaseItem: Release) {
        historyStorage.putRelease(releaseItem)
        updateHolder.viewRelease(releaseItem)
    }

    fun removeRelease(id: Int) = historyStorage.removerRelease(id)
}