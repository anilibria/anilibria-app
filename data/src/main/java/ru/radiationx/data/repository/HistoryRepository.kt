package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    private val historyRuntimeCache: HistoryRuntimeCache,
) {

    suspend fun getReleases(count: Int = Int.MAX_VALUE): List<Release> =
        withContext(Dispatchers.IO) {
            historyStorage
                .getEpisodes()
                .takeLast(count)
                .asReversed()
                .let { historyRuntimeCache.getCached(it) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeReleases(count: Int = Int.MAX_VALUE): Flow<List<Release>> = historyStorage
        .observeEpisodes()
        .map { it.takeLast(count).asReversed() }
        .flatMapLatest {
            historyRuntimeCache.observeCached(it)
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)

    suspend fun putRelease(releaseItem: Release) {
        withContext(Dispatchers.IO) {
            historyStorage.putRelease(releaseItem.id)
            updateHolder.viewRelease(releaseItem)
        }
    }

    suspend fun removeRelease(id: ReleaseId) = withContext(Dispatchers.IO) {
        historyStorage.removerRelease(id)
    }
}