package ru.radiationx.data.system

import android.os.Build
import okhttp3.OkHttp
import ru.radiationx.data.SharedBuildConfig
import javax.inject.Inject

class UserAgentGenerator @Inject constructor(
    private val buildConfig: SharedBuildConfig
) {

    val okHttpTransport by lazy { generate("okhttp/${OkHttp.VERSION}") }

    val systemTransport by lazy { generate("system/${Build.VERSION.SDK_INT}") }

    fun generate(transport: String): String {
        val appInfo = "${buildConfig.applicationName}/${buildConfig.versionName}"
        val appIdInfo = "${buildConfig.applicationId}/${buildConfig.versionCode}"
        val androidInfo = "Android ${Build.VERSION.RELEASE}/${Build.VERSION.SDK_INT}"
        val deviceInfo = "${Build.MANUFACTURER}/${Build.MODEL}"
        return "$appInfo ($appIdInfo; $androidInfo; $deviceInfo) $transport"
    }
}