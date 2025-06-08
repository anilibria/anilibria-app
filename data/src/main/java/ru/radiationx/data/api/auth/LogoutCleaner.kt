package ru.radiationx.data.api.auth

import ru.radiationx.data.api.auth.legacy.CookieHolder
import ru.radiationx.data.api.profile.UserHolder
import javax.inject.Inject

class LogoutCleaner @Inject constructor(
    private val tokenStorage: AuthTokenStorage,
    private val userHolder: UserHolder,
    private val cookieHolder: CookieHolder,
) {

    suspend fun clear() {
        tokenStorage.delete()
        userHolder.delete()
        // todo API2 migrate cookie to token
        // do not remove cookies while not migrated
        //cookieHolder.removeAuthCookie()
    }
}