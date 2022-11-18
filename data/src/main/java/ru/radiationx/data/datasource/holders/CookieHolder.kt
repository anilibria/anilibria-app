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
    fun getCookies(): Map<String, Cookie>
    fun putCookie(url: String, cookie: Cookie)
    fun putCookie(url: String, name: String, value: String)
    fun removeCookie(name: String)
}