package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.team.TeamsResponse

interface TeamsHolder {
    fun observe(): Flow<TeamsResponse>
    suspend fun get(): TeamsResponse
    suspend fun save(data: TeamsResponse)
    suspend fun delete()
}