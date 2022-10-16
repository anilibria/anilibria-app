package ru.radiationx.data.di.providers

import android.util.Log
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.system.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class ApiOkHttpProvider @Inject constructor(
    private val appCookieJar: AppCookieJar,
    private val apiConfig: ApiConfig,
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
        .apply {
            val availableAddress = apiConfig.getAddresses().map { it.tag }.contains(apiConfig.active.tag)

            if (!availableAddress) {
                val proxy = apiConfig.proxies.sortedBy { it.ping }.firstOrNull()
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
                val hostAddress =
                    it.connection()?.route()?.socketAddress()?.address?.hostAddress.orEmpty()
                /*if (!apiConfig.getPossibleIps().contains(hostAddress)) {
                    apiConfig.updateNeedConfig(true)
                    throw WrongHostException(hostAddress)
                }*/
                it.proceed(it.request()).newBuilder()
                    .header("Remote-Address", hostAddress)
                    .build()
            }

            addInterceptor {
                val additionalHeadersRequest = it.request()
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
                it.proceed(additionalHeadersRequest)
            }

            cookieJar(appCookieJar)
        }
        .build()
}