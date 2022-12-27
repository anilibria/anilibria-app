package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 30.12.17.
 */
interface AuthHolder {
    fun observeVkAuthChange(): Flow<Boolean>
    suspend fun changeVkAuth(value: Boolean)
    suspend fun getDeviceId(): String

    fun observeAuthSkipped(): Flow<Boolean>
    suspend fun getAuthSkipped(): Boolean
    suspend fun setAuthSkipped(value: Boolean)
}