package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class HistoryRuntimeCache @Inject constructor(
    private val releaseApi: ReleaseApi,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
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
        if (idsToLoad.isNotEmpty()) {
            val result = sharedRequests.request(idsToLoad) {
                releaseApi.getReleasesByIds(idsToLoad.map { it.id }).map {
                    it.toDomain(apiUtils, apiConfig)
                }
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


}