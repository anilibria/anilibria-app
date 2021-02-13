package ru.radiationx.data.di.providers

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.appendConnectionSpecs
import ru.radiationx.data.system.appendSocketFactoryIfNeeded
import ru.radiationx.data.system.appendTimeouts
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider


class MainOkHttpProvider @Inject constructor(
    private val appCookieJar: AppCookieJar,
    private val sharedBuildConfig: SharedBuildConfig
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                val level = if (sharedBuildConfig.debug) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
                setLevel(level)
            }
        )
        .appendConnectionSpecs()
        .appendSocketFactoryIfNeeded()
        .appendTimeouts()
        .addNetworkInterceptor {
            val hostAddress =
                it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
            it.proceed(it.request()).newBuilder()
                .header("Remote-Address", hostAddress)
                .build()
        }
        //.cookieJar(appCookieJar)
        .build()
        .also {
            Log.e("bobobo", "MainOkHttpProvider provide $it")
        }
}