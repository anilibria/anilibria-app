package ru.radiationx.anilibria.model.data.holders

import ru.radiationx.anilibria.entity.common.AuthState

/**
 * Created by radiationx on 30.12.17.
 */
interface AuthHolder {
    fun getAuthState(): AuthState
    fun setAuthState(state: AuthState)
}