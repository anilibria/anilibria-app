package ru.radiationx.data.di.providers

import android.util.Log
import okhttp3.Credentials
import okhttp3.OkHttpClient
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.Client
import ru.radiationx.data.system.appendConnectionSpecs
import ru.radiationx.data.system.appendSocketFactoryIfNeeded
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Provider

class ApiOkHttpProvider @Inject constructor(
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig,
        private val sharedBuildConfig: SharedBuildConfig
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
            .appendConnectionSpecs()
            .appendSocketFactoryIfNeeded()
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
                    Log.d("boboob", "hostAddress $hostAddress")
                    /*if (!apiConfig.getPossibleIps().contains(hostAddress)) {
                        apiConfig.updateNeedConfig(true)
                        throw WrongHostException(hostAddress)
                    }*/
                    it.proceed(it.request()).newBuilder()
                            .header("Remote-Address", hostAddress)
                            .build()
                }

                addInterceptor {
                    val userAgentRequest = it.request()
                            .newBuilder()
                            .header("mobileApp", "true")
                            .apply {
                                if (Api.STORE_APP_IDS.contains(sharedBuildConfig.applicationId)) {
                                    header("Store-Published", "Google")
                                }
                            }
                            .header("App-Id", sharedBuildConfig.applicationId)
                            .header("App-Ver-Name", sharedBuildConfig.versionName)
                            .header("App-Ver-Code", sharedBuildConfig.versionCode.toString())
                            .header("User-Agent", Client.USER_AGENT)
                            .build()
                    it.proceed(userAgentRequest.also {
                        Log.e("bobobo", "request url ${it.url()}")
                    })
                }

                cookieJar(appCookieJar)
            }/*
            .addNetworkInterceptor(
                    HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                        Log.d("logging", it)
                    })
                            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
            )*/
            .build()
            .also {
                Log.e("bobobo", "ApiOkHttpProvider provide $it")
            }
}