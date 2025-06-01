package ru.radiationx.data.datasource.remote.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.apinext.LogoutCleaner
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val logoutCleaner: LogoutCleaner
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            runBlocking {
                logoutCleaner.clear()
            }
        }
        return response
    }
}