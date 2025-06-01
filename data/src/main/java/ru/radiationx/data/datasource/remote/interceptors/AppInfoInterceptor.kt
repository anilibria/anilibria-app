package ru.radiationx.data.datasource.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.UserAgentGenerator
import javax.inject.Inject

class AppInfoInterceptor @Inject constructor(
    private val sharedBuildConfig: SharedBuildConfig,
    private val userAgentGenerator: UserAgentGenerator
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("mobileApp", "true")
            .header("App-Id", sharedBuildConfig.applicationId)
            .header("App-Ver-Name", sharedBuildConfig.versionName)
            .header("App-Ver-Code", sharedBuildConfig.versionCode.toString())
            .header("User-Agent", userAgentGenerator.okHttpTransport)
            .build()
        return chain.proceed(request)
    }
}