package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.types.ReleaseId

interface HistoryHolder {
    suspend fun getEpisodes(): List<ReleaseId>
    fun observeEpisodes(): Flow<List<ReleaseId>>
    suspend fun putRelease(id: ReleaseId)
    suspend fun putAllRelease(ids: List<ReleaseId>)
    suspend fun removerRelease(id: ReleaseId)
}