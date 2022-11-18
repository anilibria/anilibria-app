package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.CookieHolder.Companion.cookieNames
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class CookiesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : CookieHolder {

    private val cookiesState by lazy {
        MutableStateFlow(loadCookies())
    }

    override fun observeCookies(): Flow<Map<String, Cookie>> {
        return cookiesState.asStateFlow()
    }

    override fun getCookies(): Map<String, Cookie> {
        return cookiesState.value
    }

    override fun putCookie(url: String, name: String, value: String) {
        val domain = requireNotNull(Uri.parse(url).host) {
            "cookie domain is null"
        }
        val cookie = Cookie.Builder()
            .name(name.trim())
            .value(value.trim())
            .domain(domain)
            .build()
        putCookie(url, cookie)
    }

    override fun putCookie(url: String, cookie: Cookie) {
        sharedPreferences
            .edit()
            .putString("cookie_${cookie.name}", convertCookie(url, cookie))
            .apply()

        updateCookies()
    }

    override fun removeCookie(name: String) {
        sharedPreferences
            .edit()
            .remove("cookie_$name")
            .apply()

        updateCookies()
    }

    private fun updateCookies() {
        cookiesState.value = loadCookies()
    }

    private fun loadCookies(): Map<String, Cookie> {
        val result = mutableMapOf<String, Cookie>()
        cookieNames.forEachIndexed { _, s ->
            sharedPreferences
                .getString("cookie_$s", null)
                ?.let { parseCookie(it) }
                ?.let { cookie -> result[s] = cookie }
        }
        return result
    }

    private fun parseCookie(cookieFields: String): Cookie? {
        val fields = cookieFields.split("\\|:\\|".toRegex())
        val httpUrl = fields[0].toHttpUrlOrNull()
            ?: throw RuntimeException("Unknown cookie url = ${fields[0]}")
        val cookieString = fields[1]
        return Cookie.parse(httpUrl, cookieString)
    }

    private fun convertCookie(url: String, cookie: Cookie): String {
        return "$url|:|$cookie"
    }
}