package ru.radiationx.data.di.providers

import android.util.Log
import okhttp3.OkHttpClient
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.appendConnectionSpecs
import ru.radiationx.data.system.appendSocketFactoryIfNeeded
import javax.inject.Inject
import javax.inject.Provider


class MainOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
            .appendConnectionSpecs()
            .appendSocketFactoryIfNeeded()
            .addNetworkInterceptor {
                val hostAddress = it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
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