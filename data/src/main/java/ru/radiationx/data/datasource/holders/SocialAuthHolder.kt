package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.auth.SocialAuth

interface SocialAuthHolder {
    suspend fun get(): List<SocialAuth>
    fun observe(): Flow<List<SocialAuth>>
    suspend fun save(items: List<SocialAuth>)
    suspend fun delete()
}