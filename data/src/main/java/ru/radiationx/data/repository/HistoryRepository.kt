package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.HistoryRuntimeCache
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
    private val historyStorage: HistoryHolder,
    private val updateHolder: ReleaseUpdateHolder,
    private val historyRuntimeCache: HistoryRuntimeCache
) {

    suspend fun getReleases(): List<Release> = withContext(Dispatchers.IO) {
        historyStorage
            .getEpisodes()
            .asReversed()
            .let { historyRuntimeCache.getCached(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeReleases(): Flow<List<Release>> = historyStorage
        .observeEpisodes()
        .map { it.asReversed() }
        .flatMapLatest {
            historyRuntimeCache.observeCached(it)
        }
        .filterNotNull()
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