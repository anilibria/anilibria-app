package ru.radiationx.data.di.providers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.system.*
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Provider

class ApiOkHttpProvider @Inject constructor(
    private val context: Context,
    private val appCookieJar: AppCookieJar,
    private val apiConfig: ApiConfig,
    private val sharedBuildConfig: SharedBuildConfig
) : Provider<OkHttpClient> {

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendConnectionSpecs()
        .appendSocketFactoryIfNeeded()
        .appendTimeouts()
        .apply {
            val availableAddress =
                apiConfig.getAddresses().map { it.tag }.contains(apiConfig.active.tag)

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
        .apply {
            if (sharedBuildConfig.debug) {
                addInterceptor(HttpLoggingInterceptor())
                addInterceptor(ChuckerInterceptor.Builder(context).build())
            }
        }
        .build()
}