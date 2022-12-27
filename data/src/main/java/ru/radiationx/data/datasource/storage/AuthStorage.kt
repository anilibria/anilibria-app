package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
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
        private const val KEY_AUTH_SKIPPED = "auth_skipped"
    }

    private val vkAuthRelay = MutableSharedFlow<Boolean>()

    private val authSkippedState = SuspendMutableStateFlow {
        loadAuthSkipped()
    }

    override fun observeVkAuthChange(): Flow<Boolean> = vkAuthRelay.asSharedFlow()

    override suspend fun changeVkAuth(value: Boolean) {
        vkAuthRelay.emit(value)
    }

    override suspend fun getDeviceId(): String {
        return withContext(Dispatchers.IO) {
            var uid = sharedPreferences.getString(KEY_DEVICE_UID, null)

            if (uid == null) {
                uid = UUID.randomUUID()?.toString() ?: ""
                sharedPreferences.edit().putString(KEY_DEVICE_UID, uid).apply()
            }
            uid
        }
    }

    override fun observeAuthSkipped(): Flow<Boolean> {
        return authSkippedState
    }

    override suspend fun getAuthSkipped(): Boolean {
        return authSkippedState.getValue()
    }

    override suspend fun setAuthSkipped(value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                putBoolean(KEY_AUTH_SKIPPED, value)
            }
        }
        authSkippedState.setValue(loadAuthSkipped())
    }

    private suspend fun loadAuthSkipped(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_AUTH_SKIPPED, false)
        }
    }

}