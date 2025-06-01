package ru.radiationx.data.api.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.auth.models.AuthToken
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import toothpick.InjectConstructor

@InjectConstructor
class AuthTokenStorage(
    private val sharedPreferences: SharedPreferences,
) {

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }

    private val tokenState = SuspendMutableStateFlow {
        loadToken()
    }

    fun observe(): Flow<AuthToken?> {
        return tokenState
    }

    suspend fun get(): AuthToken? {
        return tokenState.getValue()
    }

    suspend fun save(token: AuthToken) {
        saveToken(token)
        updateState()
    }

    suspend fun delete() {
        saveToken(null)
        updateState()
    }

    private suspend fun updateState() {
        tokenState.setValue(loadToken())
    }

    private suspend fun saveToken(token: AuthToken?) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                if (token == null) {
                    remove(KEY_TOKEN)
                } else {
                    putString(KEY_TOKEN, token.token)
                }
            }
        }
    }

    private suspend fun loadToken(): AuthToken? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(KEY_TOKEN, null)?.let { AuthToken(it) }
        }
    }
}