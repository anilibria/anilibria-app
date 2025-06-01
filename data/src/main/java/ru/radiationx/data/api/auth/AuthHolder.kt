package ru.radiationx.data.api.auth

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.api.auth.models.DeviceId

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