package ru.radiationx.data.app.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.config.mapper.toDomain
import ru.radiationx.data.app.config.models.ApiAddress
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
    private val api: ConfigurationApiDataSource,
    private val apiConfigStorage: ApiConfigStorage,
) {

    suspend fun findFastest(addresses: List<ApiAddress>): ApiAddress {
        return withContext(Dispatchers.IO) {
            api.findFastest(addresses)
        }
    }

    suspend fun updateConfig() {
        withContext(Dispatchers.IO) {
            api
                .getConfiguration()
                .toDomain()
                .also { apiConfigStorage.save(it) }
        }
    }
}