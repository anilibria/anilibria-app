package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.network.appendSslCompatAnalytics
import ru.radiationx.data.network.interceptors.NetworkAvailableInterceptor
import ru.radiationx.data.network.sslcompat.SslCompat
import ru.radiationx.data.network.sslcompat.appendSslCompat
import javax.inject.Inject
import javax.inject.Provider

class PlayerOkHttpProvider @Inject constructor(
    private val context: Context,
    private val sharedBuildConfig: SharedBuildConfig,
    private val sslCompat: SslCompat,
    private val sslCompatAnalytics: SslCompatAnalytics,
    private val networkAvailableInterceptor: NetworkAvailableInterceptor
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendSslCompatAnalytics(sslCompat, sslCompatAnalytics)
        .appendSslCompat(sslCompat)
        .addInterceptor(networkAvailableInterceptor)
        .addInterceptor(BrotliInterceptor)
        .apply {
            if (sharedBuildConfig.debug) {
                addNetworkInterceptor(ChuckerInterceptor.Builder(context).build())
            }
        }
        .build()
}