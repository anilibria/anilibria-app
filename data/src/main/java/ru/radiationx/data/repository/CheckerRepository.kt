package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.api.CheckerApiDataSource
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository @Inject constructor(
    private val checkerApi: CheckerApiDataSource,
    private val sharedBuildConfig: SharedBuildConfig,
) {

    private val currentDataRelay = MutableStateFlow<UpdateData?>(null)

    fun observeUpdate(): Flow<UpdateData> = currentDataRelay.filterNotNull()

    suspend fun checkUpdate(force: Boolean = false): UpdateData {
        return withContext(Dispatchers.IO) {
            if (!force && currentDataRelay.value != null) {
                currentDataRelay.value!!
            } else {
                val currentCode = sharedBuildConfig.versionCode
                checkerApi.checkUpdate().update.toDomain(currentCode)
            }.also {
                currentDataRelay.value = it
            }
        }
    }

}
