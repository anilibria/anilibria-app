package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.network.appendSslCompatAnalytics
import ru.radiationx.data.network.appendTimeouts
import ru.radiationx.data.network.interceptors.AcceptJsonInterceptor
import ru.radiationx.data.network.interceptors.AppConfigInterceptor
import ru.radiationx.data.network.interceptors.AppInfoInterceptor
import ru.radiationx.data.network.interceptors.AuthTokenInterceptor
import ru.radiationx.data.network.interceptors.DynamicApiUrlInterceptor
import ru.radiationx.data.network.interceptors.UnauthorizedInterceptor
import ru.radiationx.data.network.sslcompat.SslCompat
import ru.radiationx.data.network.sslcompat.appendSslCompat
import javax.inject.Inject
import javax.inject.Provider

class ApiOkhttpProvider @Inject constructor(
    private val unauthorizedInterceptor: UnauthorizedInterceptor,
    private val authTokenInterceptor: AuthTokenInterceptor,
    private val appInfoInterceptor: AppInfoInterceptor,
    private val dynamicApiUrlInterceptor: DynamicApiUrlInterceptor,
    private val appConfigInterceptor: AppConfigInterceptor,
    private val context: Context,
    private val sharedBuildConfig: SharedBuildConfig,
    private val sslCompat: SslCompat,
    private val sslCompatAnalytics: SslCompatAnalytics
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendSslCompatAnalytics(sslCompat, sslCompatAnalytics)
        .appendSslCompat(sslCompat)
        .appendTimeouts()
        .addInterceptor(appConfigInterceptor)
        .addInterceptor(dynamicApiUrlInterceptor)
        .addInterceptor(BrotliInterceptor)
        .addInterceptor(AcceptJsonInterceptor())
        .addInterceptor(appInfoInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(authTokenInterceptor)
        .apply {
            if (sharedBuildConfig.debug) {
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                addNetworkInterceptor(ChuckerInterceptor.Builder(context).build())
                //eventListenerFactory(ApiLoggingEventListener.Factory())
            }
        }
        .build()
}