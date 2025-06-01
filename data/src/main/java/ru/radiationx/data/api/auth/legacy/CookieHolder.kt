package ru.radiationx.data.api.auth.legacy

import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

/**
 * Created by radiationx on 30.12.17.
 */
interface CookieHolder {
    companion object {
        const val PHPSESSID = "PHPSESSID"

        val cookieNames = listOf(
            PHPSESSID
        )
    }

    fun observeCookies(): Flow<Map<String, Cookie>>
    suspend fun getCookies(): Map<String, Cookie>
    suspend fun putCookie(url: String, cookie: Cookie)
    suspend fun removeCookie(name: String)
    suspend fun removeAuthCookie()
}