package ru.radiationx.data.datasource.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.datasource.holders.CookieHolder

class SessionCookieInterceptor : Interceptor {

    private val cookiePrefix = "${CookieHolder.PHPSESSID}="
    private val cookieSeparator = Regex(";\\s*(?=$cookiePrefix)")

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val headers = response.headers.newBuilder().removeAll("Set-Cookie")
        response.headers("Set-Cookie").forEach { header ->
            // The legacy API can fold a host-only session and an anilibria.tv session
            // into one header. Separate them before OkHttp validates cookie domains.
            val cookies = if (header.trimStart().startsWith(cookiePrefix)) {
                header.split(cookieSeparator)
            } else {
                listOf(header)
            }
            cookies.forEach { headers.add("Set-Cookie", it) }
        }
        return response.newBuilder().headers(headers.build()).build()
    }
}
