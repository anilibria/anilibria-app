package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.CheckerApi
import ru.radiationx.data.entity.app.updater.UpdateData
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val checkerApi: CheckerApi
) {

    private val currentDataRelay = MutableStateFlow<UpdateData?>(null)

    fun observeUpdate(): Flow<UpdateData> = currentDataRelay.filterNotNull()

    suspend fun checkUpdate(versionCode: Int, force: Boolean = false): UpdateData {
        return if (!force && currentDataRelay.value != null) {
            currentDataRelay.value!!
        } else {
            checkerApi.checkUpdate(versionCode)
        }.also {
            currentDataRelay.value = it
        }
    }

}
