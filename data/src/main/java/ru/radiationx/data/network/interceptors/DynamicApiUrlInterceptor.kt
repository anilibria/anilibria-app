package ru.radiationx.data.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.app.config.ConfiguringInteractor
import ru.radiationx.data.common.toBaseUrl
import ru.radiationx.data.common.toPathUrl
import timber.log.Timber
import javax.inject.Inject

class DynamicApiUrlInterceptor @Inject constructor(
    private val apiConfig: ApiConfig,
    private val configuringInteractor: ConfiguringInteractor
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

        Timber.tag(TAG).d("awaitActualConfig start ${apiConfig.id} for $endpoint")
        runBlocking {
            configuringInteractor.configure()
        }
        Timber.tag(TAG).d("awaitActualConfig end ${apiConfig.id} for $endpoint")

        val apiVersion = apiConfig.api.withPath(API_PREFIX).toBaseUrl()
        val newUrl = endpoint.withBase(apiVersion)

        Timber.tag(TAG).d("change url ${url} -> ${newUrl}")
        val newRequest = request.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }

}