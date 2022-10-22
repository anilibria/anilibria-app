package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.AuthHolder
import java.util.*
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : AuthHolder {

    companion object {
        private const val KEY_DEVICE_UID = "device_uid"
    }

    private val vkAuthRelay = MutableSharedFlow<Boolean>()

    override fun observeVkAuthChange(): Flow<Boolean> = vkAuthRelay.asSharedFlow()

    override suspend fun changeVkAuth(value: Boolean) {
        vkAuthRelay.emit(value)
    }

    override fun getDeviceId(): String {
        var uid = sharedPreferences.getString(KEY_DEVICE_UID, null)

        if (uid == null) {
            uid = UUID.randomUUID()?.toString() ?: ""
            sharedPreferences.edit().putString(KEY_DEVICE_UID, uid).apply()
        }

        return uid
    }
}