package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.holders.MenuHolder
import ru.radiationx.data.datasource.remote.api.MenuApi
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

class MenuRepository @Inject constructor(
    private val menuHolder: MenuHolder,
    private val menuApi: MenuApi,
) {

    fun observeMenu(): Flow<List<LinkMenuItem>> = menuHolder
        .observe()
        .flowOn(Dispatchers.IO)

    suspend fun getMenu(): List<LinkMenuItem> = withContext(Dispatchers.IO) {
        menuApi
            .getMenu()
            .map { it.toDomain() }
            .also { menuHolder.save(it) }
    }
}