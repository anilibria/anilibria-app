package ru.radiationx.data.datasource.holders

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
    suspend fun putCookie(url: String, name: String, value: String)
    suspend fun removeCookie(name: String)
}