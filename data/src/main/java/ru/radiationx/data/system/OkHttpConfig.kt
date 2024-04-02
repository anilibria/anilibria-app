package ru.radiationx.data.system

import okhttp3.OkHttpClient
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.sslcompat.SslCompat
import java.util.concurrent.TimeUnit

fun OkHttpClient.Builder.appendSslCompatAnalytics(
    sslCompat: SslCompat,
    sslCompatAnalytics: SslCompatAnalytics,
): OkHttpClient.Builder {
    sslCompatAnalytics.oneShotError(sslCompat.data)
    return this
}

fun OkHttpClient.Builder.appendTimeouts(): OkHttpClient.Builder {
    callTimeout(25, TimeUnit.SECONDS)
    connectTimeout(15, TimeUnit.SECONDS)
    readTimeout(15, TimeUnit.SECONDS)
    writeTimeout(15, TimeUnit.SECONDS)
    return this
}
