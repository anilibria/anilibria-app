package ru.radiationx.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.network.NetworkObserver
import timber.log.Timber
import javax.inject.Inject


class NetworkAvailableInterceptor @Inject constructor(
    private val networkObserver: NetworkObserver
) : SuspendableInterceptor() {

    override suspend fun interceptSuspend(chain: Interceptor.Chain): Response {
        if (!networkObserver.isAvailable()) {
            Timber.tag("NetworkAvailableInterceptor").i("Waiting network")
        }
        networkObserver.awaitAvailable()
        return chain.proceed(chain.request())
    }
}
