package ru.radiationx.data.network

import okhttp3.OkHttpClient
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.network.sslcompat.SslCompat
import java.util.concurrent.TimeUnit

fun OkHttpClient.Builder.appendSslCompatAnalytics(
    sslCompat: SslCompat,
    sslCompatAnalytics: SslCompatAnalytics,
): OkHttpClient.Builder {
    sslCompatAnalytics.oneShotError(sslCompat.data)
    return this
}

fun OkHttpClient.Builder.appendTimeouts(): OkHttpClient.Builder {
    connectTimeout(5, TimeUnit.SECONDS)
    readTimeout(5, TimeUnit.SECONDS)
    writeTimeout(5, TimeUnit.SECONDS)
    return this
}
