package ru.radiationx.anilibria.model.datasource.holders

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

    fun getCookies(): Map<String, Cookie>
    fun putCookie(url: String, cookie: Cookie)
    fun putCookie(url: String, name: String, value: String)
    fun removeCookie(name: String)
}