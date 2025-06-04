package ru.radiationx.data.network.interceptors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

abstract class SuspendableInterceptor : Interceptor {

    open val watcherDelay: Long get() = 200

    final override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        val interceptJob = async(Dispatchers.IO) {
            interceptSuspend(chain)
        }

        launch(Dispatchers.IO) {
            runCatching {
                while (interceptJob.isActive) {
                    if (chain.call().isCanceled()) {
                        runCatching {
                            interceptJob.cancel("OkHttp call canceled")
                        }
                    }
                    delay(watcherDelay)
                }
            }
        }

        runCatching { interceptJob.await() }.getOrElse {
            throw IOException("Intercept job failed", it)
        }
    }

    abstract suspend fun interceptSuspend(chain: Interceptor.Chain): Response
}