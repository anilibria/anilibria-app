package ru.radiationx.data.app.menu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.menu.mapper.toDomain
import ru.radiationx.data.app.menu.models.LinkMenuItem
import javax.inject.Inject

class MenuRepository @Inject constructor(
    private val menuHolder: MenuHolder,
    private val menuApi: MenuApiDataSource,
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