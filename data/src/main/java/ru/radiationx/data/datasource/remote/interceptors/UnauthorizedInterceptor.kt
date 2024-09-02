package ru.radiationx.data.datasource.remote.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.apinext.AuthTokenStorage
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.UserHolder
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val tokenStorage: AuthTokenStorage,
    private val userHolder: UserHolder,
    private val cookieHolder: CookieHolder,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            runBlocking {
                tokenStorage.delete()
                userHolder.delete()
                cookieHolder.removeAuthCookie()
            }
        }
        return response
    }
}