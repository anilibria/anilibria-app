package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.AuthHolder

/**
 * Created by radiationx on 30.12.17.
 */
class AuthStorage constructor(private val sharedPreferences: SharedPreferences) : AuthHolder {

    override fun getAuthState(): AuthState {
        val savedState = sharedPreferences.getString("auth_state", AuthState.NO_AUTH.toString())
        return AuthState.valueOf(savedState)
    }

    override fun setAuthState(state: AuthState) {
        sharedPreferences
                .edit()
                .putString("auth_state", state.toString())
                .apply()
    }
}