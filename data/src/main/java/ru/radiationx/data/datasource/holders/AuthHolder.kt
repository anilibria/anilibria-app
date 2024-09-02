package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.apinext.models.DeviceId

/**
 * Created by radiationx on 30.12.17.
 */
interface AuthHolder {
    fun observeVkAuthChange(): Flow<Boolean>
    suspend fun changeVkAuth(value: Boolean)
    suspend fun getDeviceId(): DeviceId

    fun observeAuthSkipped(): Flow<Boolean>
    suspend fun getAuthSkipped(): Boolean
    suspend fun setAuthSkipped(value: Boolean)
}