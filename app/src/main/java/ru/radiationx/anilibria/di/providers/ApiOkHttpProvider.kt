package ru.radiationx.anilibria.di.providers

import android.util.Log
import okhttp3.OkHttpClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.system.AppCookieJar
import ru.radiationx.anilibria.model.system.Client
import ru.radiationx.anilibria.model.system.WrongHostException
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Provider
import android.R.attr.password
import okhttp3.Credentials
import ru.radiationx.anilibria.BuildConfig


class ApiOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
            .apply {
                val availableAddress = apiConfig.getAvailableAddresses().contains(apiConfig.active.tag)

                Log.d("bobobo", "create OkHttpClient with address ${apiConfig.active.tag}, available=$availableAddress")
                if (!availableAddress) {
                    val proxy = apiConfig.proxies.sortedBy { it.ping }.firstOrNull()
                    Log.d("bobobo", "create OkHttpClient with proxy $proxy")
                    proxy?.also {
                        proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(it.ip, it.port)))
                        val username = it.user
                        val password = it.password
                        if (username != null && password != null) {
                            proxyAuthenticator { route, response ->
                                val credential = Credentials.basic(username, password)
                                response.request().newBuilder()
                                        .header("Proxy-Authorization", credential)
                                        .build()
                            }
                        }
                    }
                }


                addNetworkInterceptor {
                    val hostAddress = it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
                    Log.d("boboob", "hostAddress $hostAddress, possible=${apiConfig.getPossibleIps()}")
                    if (!apiConfig.getPossibleIps().contains(hostAddress)) {
                        apiConfig.updateNeedConfig(true)
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
                            .apply {
                                if (BuildConfig.FLAVOR == "store") {
                                    header("Store-Published", "Google")
                                }
                            }
                            .header("User-Agent", Client.USER_AGENT)
                            .build()
                    it.proceed(userAgentRequest.also {
                        Log.e("bobobo", "request url ${it.url()}")
                    })
                }

                cookieJar(appCookieJar)
            }
            .build()
            .also {
                Log.e("bobobo", "ApiOkHttpProvider provide $it")
            }
}