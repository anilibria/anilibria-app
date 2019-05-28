package ru.radiationx.anilibria.di.providers

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.system.AppCookieJar
import ru.radiationx.anilibria.model.system.Client
import ru.radiationx.anilibria.model.system.ClientWrapper
import ru.radiationx.anilibria.model.system.WrongHostException
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Provider

class ApiOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Provider<ClientWrapper> {

    override fun get(): ClientWrapper = OkHttpClient.Builder()
            .apply {
                val proxy = apiConfig.proxies.sortedBy { it.ping }.firstOrNull()
                Log.d("bobobo", "create OkHttpClient with proxy $proxy")
                proxy?.also {
                    proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(it.ip, it.port)))
                }

                addNetworkInterceptor {
                    val hostAddress = it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
                    Log.d("boboob", "hostAddress $hostAddress, possible=${apiConfig.getPossibleIps()}")
                    if (!apiConfig.getPossibleIps().contains(hostAddress)) {
                        throw WrongHostException(hostAddress)
                    }
                    it.proceed(it.request()).newBuilder()
                            .header("Remote-Address", hostAddress)
                            .build()
                }

                addInterceptor {
                    val userAgentRequest = it.request()
                            .newBuilder()
                            .header("mobileApp", "true")
                            .header("User-Agent", Client.USER_AGENT)
                            .build()
                    it.proceed(userAgentRequest)
                }

                cookieJar(appCookieJar)
            }
            .build()
            .let { ClientWrapper(it) }
}