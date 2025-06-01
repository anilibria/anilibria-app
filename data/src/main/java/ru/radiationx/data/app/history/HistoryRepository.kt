package ru.radiationx.data.app.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.history.models.HistoryReleases
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateHolder
import ru.radiationx.data.common.ReleaseId
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryRepository @Inject constructor(
    private val historyStorage: HistoryHolder,
    private val updateHolder: ReleaseUpdateHolder,
    private val historyRuntimeCache: HistoryRuntimeCache,
) {

    suspend fun getReleases(count: Int = Int.MAX_VALUE): HistoryReleases =
        withContext(Dispatchers.IO) {
            val allIds = historyStorage.getIds()
            val trimmedReleases = allIds
                .takeLast(count)
                .asReversed()
                .let { historyRuntimeCache.getCached(it) }
            HistoryReleases(trimmedReleases, allIds.size)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeReleases(count: Int = Int.MAX_VALUE): Flow<HistoryReleases> = historyStorage
        .observeIds()
        .map { it.takeLast(count).asReversed() to it.size }
        .flatMapLatest { (allIds, total) ->
            historyRuntimeCache.observeCached(allIds).map { releases ->
                HistoryReleases(releases, total)
            }
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)

    suspend fun putReleaseId(id: ReleaseId) {
        withContext(Dispatchers.IO) {
            historyStorage.putId(id)
        }
    }

    suspend fun putRelease(releaseItem: Release) {
        withContext(Dispatchers.IO) {
            historyStorage.putId(releaseItem.id)
            updateHolder.viewRelease(releaseItem)
        }
    }

    suspend fun removeRelease(id: ReleaseId) = withContext(Dispatchers.IO) {
        historyStorage.removeId(id)
    }
}