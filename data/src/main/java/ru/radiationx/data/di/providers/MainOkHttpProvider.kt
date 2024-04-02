package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.sslcompat.SslCompat
import ru.radiationx.data.sslcompat.appendSslCompat
import ru.radiationx.data.system.appendSslCompatAnalytics
import ru.radiationx.data.system.appendTimeouts
import javax.inject.Inject
import javax.inject.Provider


class MainOkHttpProvider @Inject constructor(
    private val context: Context,
    private val sharedBuildConfig: SharedBuildConfig,
    private val sslCompat: SslCompat,
    private val sslCompatAnalytics: SslCompatAnalytics
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendSslCompatAnalytics(sslCompat, sslCompatAnalytics)
        .appendSslCompat(sslCompat)
        .appendTimeouts()
        .addNetworkInterceptor {
            val hostAddress =
                it.connection()?.route()?.socketAddress?.address?.hostAddress.orEmpty()
            it.proceed(it.request()).newBuilder()
                .header("Remote-Address", hostAddress)
                .build()
        }
        .apply {
            if (sharedBuildConfig.debug) {
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                addNetworkInterceptor(ChuckerInterceptor.Builder(context).build())
            }
        }
        .build()
}