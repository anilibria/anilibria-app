package ru.radiationx.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.app.config.AppConfig
import ru.radiationx.data.common.toBaseUrl
import ru.radiationx.data.common.toPathUrl
import timber.log.Timber
import javax.inject.Inject

class DynamicApiUrlInterceptor @Inject constructor(
    private val appConfig: AppConfig
) : Interceptor {

    companion object {
        private const val TAG = "DynamicApiUrlInterceptor"
        private val API_PREFIX = "/api/v1/".toPathUrl()
        val BASE_URL = "http://localhost/placeholder/".toBaseUrl()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        if (!url.startsWith(BASE_URL.value)) {
            return chain.proceed(request)
        }

        val endpoint = url.removePrefix(BASE_URL.value).toPathUrl()
        val apiVersion = appConfig.api.withPath(API_PREFIX).toBaseUrl()
        val newUrl = endpoint.withBase(apiVersion)

        Timber.tag(TAG).d("change url $url -> $newUrl")

        val newRequest = request.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }

}