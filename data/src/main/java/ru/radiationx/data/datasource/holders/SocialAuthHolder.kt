package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.auth.SocialAuth

interface SocialAuthHolder {
    fun get(): List<SocialAuth>
    fun observe(): Flow<List<SocialAuth>>
    fun save(items: List<SocialAuth>)
    fun delete()
}