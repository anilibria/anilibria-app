package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 30.12.17.
 */
interface AuthHolder {
    fun observeVkAuthChange(): Flow<Boolean>
    suspend fun changeVkAuth(value: Boolean)
    fun getDeviceId(): String

    fun observeAuthSkipped(): Flow<Boolean>
    fun getAuthSkipped(): Boolean
    fun setAuthSkipped(value: Boolean)
}