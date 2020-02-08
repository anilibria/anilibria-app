package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import okhttp3.Cookie
import okhttp3.HttpUrl
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.CookieHolder.Companion.cookieNames
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class CookiesStorage @Inject constructor(
        private val sharedPreferences: SharedPreferences
) : CookieHolder {

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
        val httpUrl = HttpUrl.parse(fields[0])
                ?: throw RuntimeException("Unknown cookie url = ${fields[0]}")
        val cookieString = fields[1]
        return Cookie.parse(httpUrl, cookieString)
    }

    private fun convertCookie(url: String, cookie: Cookie): String {
        return "$url|:|$cookie"
    }

    override fun getCookies(): Map<String, Cookie> {
        Log.e("CookiesStorage", "getCookies: ${clientCookies.map { it.value }.joinToString { "${it.name()}=${it.value()}" }}")
        return clientCookies
    }

    override fun putCookie(url: String, name: String, value: String) {
        putCookie(url, Cookie.Builder().name(name.trim()).value(value.trim()).domain(Uri.parse(url).host).build())
    }

    override fun putCookie(url: String, cookie: Cookie) {
        Log.e("CookiesStorage", "putCookie: ${"${cookie.name()}=${cookie.value()}"}")
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