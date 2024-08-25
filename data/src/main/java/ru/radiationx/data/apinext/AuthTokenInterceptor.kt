package ru.radiationx.data.apinext

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import toothpick.InjectConstructor

@InjectConstructor
class AuthTokenInterceptor(
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