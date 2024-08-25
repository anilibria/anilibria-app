package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import ru.radiationx.data.apinext.datasources.ReleasesApiDataSource
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class HistoryRuntimeCache @Inject constructor(
    private val releaseApi: ReleasesApiDataSource,
) {

    private val cachedData = MutableStateFlow<Map<ReleaseId, Release>>(emptyMap())

    private val sharedRequests = SharedRequests<Set<ReleaseId>, List<Release>>()

    fun observeCached(ids: List<ReleaseId>): Flow<List<Release>> {
        return cachedData
            .onStart {
                coRunCatching {
                    decideToLoad(ids)
                }
            }
            .map { cacheMap ->
                ids.mapNotNull { id ->
                    cacheMap[id]
                }
            }
            .distinctUntilChanged()
    }

    suspend fun getCached(ids: List<ReleaseId>): List<Release> {
        decideToLoad(ids)
        return cachedData.value.let { cacheMap ->
            ids.mapNotNull { id ->
                cacheMap[id]
            }
        }
    }

    private suspend fun decideToLoad(ids: List<ReleaseId>) {
        val idsSet = ids.toSet()
        val cachedIdsSet = cachedData.value.keys
        val idsToLoad = idsSet - cachedIdsSet
        if (idsToLoad.isEmpty()) return

        val result = sharedRequests.request(idsToLoad) {
            releaseApi.getReleasesByIds(idsToLoad.toList())
        }

        cachedData.update { cacheMap ->
            val resultMap = cacheMap.toMutableMap()
            result.forEach {
                resultMap[it.id] = it
            }
            resultMap
        }
    }

}