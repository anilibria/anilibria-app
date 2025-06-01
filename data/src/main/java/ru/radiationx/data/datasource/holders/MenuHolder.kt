package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.db.LinkMenuDb
import ru.radiationx.data.entity.domain.other.LinkMenuItem

interface MenuHolder {
    fun observe(): Flow<List<LinkMenuItem>>
    suspend fun get(): List<LinkMenuItem>
    suspend fun save(items: List<LinkMenuItem>)
}