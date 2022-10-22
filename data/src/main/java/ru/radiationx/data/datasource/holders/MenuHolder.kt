package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.other.LinkMenuItem

interface MenuHolder {
    fun observe(): Flow<List<LinkMenuItem>>
    fun save(items: List<LinkMenuItem>)
    fun get(): List<LinkMenuItem>
}