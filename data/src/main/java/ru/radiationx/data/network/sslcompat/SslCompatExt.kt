package ru.radiationx.data.network.sslcompat

import okhttp3.OkHttpClient
import timber.log.Timber


fun OkHttpClient.Builder.appendSslCompat(sslCompat: SslCompat): OkHttpClient.Builder {
    val data = sslCompat.data.getOrNull() ?: return this
    try {
        connectionSpecs(data.connectionSpec)
        sslSocketFactory(data.socketFactory, data.trustManager)
    } catch (e: Exception) {
        Timber.e(e)
    }
    return this
}