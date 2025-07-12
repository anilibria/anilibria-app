package ru.radiationx.data.api.auth

import ru.radiationx.data.api.profile.UserHolder
import javax.inject.Inject

class LogoutCleaner @Inject constructor(
    private val tokenStorage: AuthTokenStorage,
    private val userHolder: UserHolder,
) {

    suspend fun clear() {
        tokenStorage.delete()
        userHolder.delete()
    }
}