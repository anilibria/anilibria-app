package ru.radiationx.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.app.config.AppConfigConfigurator
import javax.inject.Inject


class AppConfigInterceptor @Inject constructor(
    private val appConfigConfigurator: AppConfigConfigurator
) : SuspendableInterceptor() {

    override suspend fun interceptSuspend(chain: Interceptor.Chain): Response {
        appConfigConfigurator.configure()
        return chain.proceed(chain.request())
    }
}
