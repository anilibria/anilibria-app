package ru.radiationx.data.api.auth.legacy

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class CookiesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : CookieHolder {

    companion object {
        private const val PHPSESSID = "PHPSESSID"
    }

    private val cookiesState = SuspendMutableStateFlow {
        loadCookies()
    }

    override suspend fun getAuthCookie(): Cookie? {
        return cookiesState.getValue()[PHPSESSID]
    }

    override suspend fun removeAuthCookie() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                remove("cookie_$PHPSESSID")
            }
        }
        cookiesState.setValue(loadCookies())
    }

    private suspend fun loadCookies(): Map<String, Cookie> {
        return withContext(Dispatchers.IO) {
            val result = mutableMapOf<String, Cookie>()
            sharedPreferences
                .getString("cookie_$PHPSESSID", null)
                ?.let { parseCookie(it) }
                ?.let { cookie -> result[PHPSESSID] = cookie }
            result
        }
    }

    private fun parseCookie(cookieFields: String): Cookie? {
        val fields = cookieFields.split("\\|:\\|".toRegex())
        val httpUrl = fields[0].toHttpUrlOrNull() ?: return null
        val cookieString = fields[1]
        return Cookie.parse(httpUrl, cookieString)
    }

}