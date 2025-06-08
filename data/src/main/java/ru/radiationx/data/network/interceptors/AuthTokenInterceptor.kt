package ru.radiationx.data.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.api.auth.AuthTokenStorage
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val tokenStorage: AuthTokenStorage
) : Interceptor {

    companion object {
        const val HEADER_AUTH = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val token = tokenStorage.get() ?: return@runBlocking chain.proceed(chain.request())
            val builder = chain
                .request()
                .newBuilder()
                .addHeader(HEADER_AUTH, "Bearer ${token.token}")
            chain.proceed(builder.build())
        }
    }
}