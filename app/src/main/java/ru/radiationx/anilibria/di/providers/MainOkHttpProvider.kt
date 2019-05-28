package ru.radiationx.anilibria.di.providers

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.anilibria.model.system.AppCookieJar
import ru.radiationx.anilibria.model.system.ClientWrapper
import ru.radiationx.anilibria.model.system.WrongHostException
import javax.inject.Inject
import javax.inject.Provider

class MainOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar
) : Provider<ClientWrapper> {

    override fun get(): ClientWrapper = OkHttpClient.Builder()
            .addNetworkInterceptor {
                val hostAddress = it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
                it.proceed(it.request()).newBuilder()
                        .header("Remote-Address", hostAddress)
                        .build()
            }
            .cookieJar(appCookieJar)
            .build()
            .let { ClientWrapper(it) }
}