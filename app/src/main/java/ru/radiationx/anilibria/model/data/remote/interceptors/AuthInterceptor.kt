package ru.radiationx.anilibria.model.data.remote.interceptors

/**
 * Created by radiationx on 30.12.17.
 */
/*
class AuthInterceptor constructor(private val authHolder: AuthHolder) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        authHolder.getAuthToken()?.let {
            val newUrl = request
                    .url()
                    .newBuilder()
                    .addQueryParameter(ApiParams.TOKEN, authHolder.getAuthToken())
                    .build()

            request = request.newBuilder().url(newUrl).build()
        }

        return chain.proceed(request)
    }
}*/
