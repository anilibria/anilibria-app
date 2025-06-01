package ru.radiationx.data.app.history

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.common.ReleaseId

interface HistoryHolder {
    suspend fun getIds(): List<ReleaseId>
    fun observeIds(): Flow<List<ReleaseId>>
    suspend fun putId(id: ReleaseId)
    suspend fun putAllIds(ids: List<ReleaseId>)
    suspend fun removeId(id: ReleaseId)
}