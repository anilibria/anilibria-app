package ru.radiationx.anilibria.di.providers

import android.util.Log
import okhttp3.OkHttpClient
import ru.radiationx.anilibria.model.system.AppCookieJar
import javax.inject.Inject
import javax.inject.Provider

class MainOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor {
                val hostAddress = it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
                it.proceed(it.request()).newBuilder()
                        .header("Remote-Address", hostAddress)
                        .build()
            }
            .cookieJar(appCookieJar)
            .build()
            .also {
                Log.e("bobobo", "MainOkHttpProvider provide $it")
            }
}