package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.appendConnectionSpecs
import ru.radiationx.data.system.appendSocketFactoryIfNeeded
import ru.radiationx.data.system.appendTimeouts
import javax.inject.Inject
import javax.inject.Provider


class MainOkHttpProvider @Inject constructor(
    private val context: Context,
    private val appCookieJar: AppCookieJar,
    private val sharedBuildConfig: SharedBuildConfig
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendConnectionSpecs()
        .appendSocketFactoryIfNeeded()
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
                addInterceptor(HttpLoggingInterceptor())
                addInterceptor(ChuckerInterceptor.Builder(context).build())
            }
        }
        .build()
}