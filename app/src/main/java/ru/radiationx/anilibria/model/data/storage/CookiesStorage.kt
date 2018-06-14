package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import android.util.Log
import okhttp3.Cookie
import okhttp3.HttpUrl
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.CookieHolder.Companion.cookieNames

/**
 * Created by radiationx on 30.12.17.
 */
class CookiesStorage(private val sharedPreferences: SharedPreferences) : CookieHolder {

    private val clientCookies = mutableMapOf<String, Cookie>()

    init {
        cookieNames.forEachIndexed { _, s ->
            val savedCookie = sharedPreferences.getString("cookie_$s", null)
            Log.e("CookiesStorage", "cookieName: $s : $savedCookie")
            savedCookie?.let {
                val cookie = parseCookie(it)
                Log.e("CookiesStorage", "initial putCookie: ${cookie?.name()}")
                cookie?.let { it1 -> clientCookies.put(s, it1) }
            }
        }
    }

    private fun parseCookie(cookieFields: String): Cookie? {
        val fields = cookieFields.split("\\|:\\|".toRegex())
        return Cookie.parse(HttpUrl.parse(fields[0]), fields[1])
    }

    private fun convertCookie(url: String, cookie: Cookie): String {
        return "$url|:|$cookie"
    }

    override fun getCookies(): Map<String, Cookie> {
        Log.e("CookiesStorage", "getCookies: ${clientCookies.map { it.value.name() }.joinToString { it }}")
        return clientCookies
    }

    override fun putCookie(url: String, cookie: Cookie) {
        Log.e("CookiesStorage", "putCookie: ${cookie.name()}")
        sharedPreferences
                .edit()
                .putString("cookie_${cookie.name()}", convertCookie(url, cookie))
                .apply()

        if (!clientCookies.containsKey(cookie.name())) {
            clientCookies.remove(cookie.name())
        }
        clientCookies[cookie.name()] = cookie
    }

    override fun removeCookie(name: String) {
        sharedPreferences
                .edit()
                .remove("cookie_$name")
                .apply()

        clientCookies.remove(name)
    }
}