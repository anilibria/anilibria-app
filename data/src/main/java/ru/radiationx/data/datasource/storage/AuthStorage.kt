package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.*
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
        private const val KEY_AUTH_SKIPPED = "auth_skipped"
    }

    private val vkAuthRelay = MutableSharedFlow<Boolean>()

    private val authSkippedState by lazy {
        MutableStateFlow(loadAuthSkipped())
    }

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

    override fun observeAuthSkipped(): Flow<Boolean> {
        return authSkippedState.asStateFlow()
    }

    override fun getAuthSkipped(): Boolean {
        return authSkippedState.value
    }

    override fun setAuthSkipped(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_AUTH_SKIPPED, value)
        }
        authSkippedState.value = loadAuthSkipped()
    }

    private fun loadAuthSkipped(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTH_SKIPPED, false)
    }

}