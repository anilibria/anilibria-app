package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
    private val historyStorage: HistoryHolder,
    private val updateHolder: ReleaseUpdateHolder
) {

    suspend fun getReleases(): List<Release> = withContext(Dispatchers.IO) {
        historyStorage
            .getEpisodes()
            .asReversed()
    }

    fun observeReleases(): Flow<List<Release>> = historyStorage
        .observeEpisodes()
        .map { it.asReversed() }
        .flowOn(Dispatchers.IO)

    suspend fun putRelease(releaseItem: Release) {
        withContext(Dispatchers.IO) {
            historyStorage.putRelease(releaseItem)
            updateHolder.viewRelease(releaseItem)
        }
    }

    suspend fun removeRelease(id: ReleaseId) = withContext(Dispatchers.IO) {
        historyStorage.removerRelease(id)
    }
}