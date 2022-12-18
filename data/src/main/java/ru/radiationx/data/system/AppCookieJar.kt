package ru.radiationx.data.system

import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.UserHolder
import javax.inject.Inject

class AppCookieJar @Inject constructor(
    private val cookieHolder: CookieHolder,
    private val userHolder: UserHolder
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        runBlocking {
            var authDestroyed = false
            for (cookie in cookies) {
                if (cookie.value == "deleted") {
                    if (cookie.name == CookieHolder.PHPSESSID) {
                        authDestroyed = true
                    }
                    cookieHolder.removeCookie(cookie.name)
                } else {
                    cookieHolder.putCookie(url.toString(), cookie)
                }
            }
            if (authDestroyed) {
                userHolder.delete()
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return runBlocking {
            cookieHolder.getCookies().values.map { it }
        }
    }
}