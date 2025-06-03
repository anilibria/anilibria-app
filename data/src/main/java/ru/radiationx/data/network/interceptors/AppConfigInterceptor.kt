package ru.radiationx.data.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.app.config.AppConfigUpdater
import javax.inject.Inject


class AppConfigInterceptor @Inject constructor(
    private val appConfigUpdater: AppConfigUpdater
) : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            appConfigUpdater.update()
            chain.proceed(chain.request())
        }
    }
}
