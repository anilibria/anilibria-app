package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import javax.inject.Inject
import javax.inject.Provider

class SimpleOkHttpProvider @Inject constructor(
    private val context: Context,
    private val sharedBuildConfig: SharedBuildConfig,
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (sharedBuildConfig.debug) {
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                addNetworkInterceptor(ChuckerInterceptor.Builder(context).build())
            }
        }
        .build()
}