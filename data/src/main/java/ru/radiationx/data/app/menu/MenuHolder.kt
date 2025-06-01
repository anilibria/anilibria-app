package ru.radiationx.data.app.menu

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.app.menu.models.LinkMenuItem

interface MenuHolder {
    fun observe(): Flow<List<LinkMenuItem>>
    suspend fun get(): List<LinkMenuItem>
    suspend fun save(items: List<LinkMenuItem>)
}