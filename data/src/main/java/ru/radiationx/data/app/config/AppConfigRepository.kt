package ru.radiationx.data.app.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.config.mapper.toDomain
import ru.radiationx.data.app.config.models.AppConfigAddress
import javax.inject.Inject

class AppConfigRepository @Inject constructor(
    private val api: AppConfigApiDataSource,
    private val storage: AppConfigStorage,
) {

    suspend fun findFastest(addresses: List<AppConfigAddress>): AppConfigAddress {
        return withContext(Dispatchers.IO) {
            api.findFastest(addresses)
        }
    }

    suspend fun updateConfig() {
        withContext(Dispatchers.IO) {
            api
                .getConfig()
                .toDomain()
                .also { storage.save(it) }
        }
    }
}