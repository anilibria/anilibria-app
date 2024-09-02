package ru.radiationx.data.apinext

import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.UserHolder
import toothpick.InjectConstructor

@InjectConstructor
class LogoutCleaner(
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