package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.CookieHolder.Companion.cookieNames
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class CookiesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : CookieHolder {

    private val cookiesState = SuspendMutableStateFlow {
        loadCookies()
    }

    override fun observeCookies(): Flow<Map<String, Cookie>> {
        return cookiesState
    }

    override suspend fun getCookies(): Map<String, Cookie> {
        return cookiesState.getValue()
    }

    override suspend fun putCookie(url: String, cookie: Cookie) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .putString("cookie_${cookie.name}", convertCookie(url, cookie))
                .apply()
        }
        updateCookies()
    }

    override suspend fun removeCookie(name: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences
                .edit()
                .remove("cookie_$name")
                .apply()
        }
        updateCookies()
    }

    override suspend fun removeAuthCookie() {
        removeCookie(CookieHolder.PHPSESSID)
    }

    private suspend fun updateCookies() {
        cookiesState.setValue(loadCookies())
    }

    private suspend fun loadCookies(): Map<String, Cookie> {
        return withContext(Dispatchers.IO) {
            val result = mutableMapOf<String, Cookie>()
            cookieNames.forEachIndexed { _, s ->
                sharedPreferences
                    .getString("cookie_$s", null)
                    ?.let { parseCookie(it) }
                    ?.let { cookie -> result[s] = cookie }
            }
            result
        }
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