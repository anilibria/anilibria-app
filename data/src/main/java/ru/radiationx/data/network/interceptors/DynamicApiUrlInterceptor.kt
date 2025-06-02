package ru.radiationx.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.common.toBaseUrl
import ru.radiationx.data.common.toPathUrl
import javax.inject.Inject

class DynamicApiUrlInterceptor @Inject constructor(
    private val apiConfig: ApiConfig
) : Interceptor {

    companion object {
        val BASE_URL = "http://localhost/api/v1/".toBaseUrl()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        if (!url.startsWith(BASE_URL.value)) {
            return chain.proceed(request)
        }
        val endpoint = url.removePrefix(BASE_URL.value).toPathUrl()
        val newUrl = endpoint.withBase(apiConfig.apiUrl)
        val newRequest = request.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}