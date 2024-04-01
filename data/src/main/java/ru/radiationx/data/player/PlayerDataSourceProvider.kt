package ru.radiationx.data.player

import android.content.Context
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttp
import org.chromium.net.CronetEngine
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.di.providers.PlayerOkHttpProvider
import ru.radiationx.data.entity.common.PlayerTransport
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class PlayerDataSourceProvider @Inject constructor(
    private val context: Context,
    private val playerOkHttpProvider: PlayerOkHttpProvider,
    private val preferencesHolder: PreferencesHolder,
    private val buildConfig: SharedBuildConfig,
) {

    private val cronetThreadPool by lazy { Executors.newFixedThreadPool(4) }

    @UnstableApi
    fun get(): DataSourceType {
        return when (preferencesHolder.playerTransport.value) {
            PlayerTransport.SYSTEM -> createSystem()
            PlayerTransport.OKHTTP -> createOkhttp()
            PlayerTransport.CRONET -> createCronet() ?: createOkhttp()
        }
    }

    @UnstableApi
    private fun createSystem(): DataSourceType {
        val factory = DefaultHttpDataSource.Factory().apply {
            setUserAgent(createUserAgent("system/${Build.VERSION.SDK_INT}"))
        }
        return DataSourceType(factory, PlayerTransport.SYSTEM)
    }

    @UnstableApi
    private fun createOkhttp(): DataSourceType {
        val factory = OkHttpDataSource.Factory(playerOkHttpProvider.get()).apply {
            setUserAgent(createUserAgent("okhttp/${OkHttp.VERSION}"))
        }
        return DataSourceType(factory, PlayerTransport.OKHTTP)
    }

    @UnstableApi
    private fun createCronet(): DataSourceType? {
        val cronet = try {
            CronetEngine.Builder(context)
                .enableHttp2(true)
                .build()
        } catch (ex: Throwable) {
            Timber.e(ex, "tryCronet")
            return null
        }

        val factory = CronetDataSource.Factory(cronet, cronetThreadPool).apply {
            setUserAgent(createUserAgent(cronet.versionString ?: "cronet/unknown"))
        }

        return DataSourceType(factory, PlayerTransport.CRONET)
    }

    private fun createUserAgent(transport: String): String {
        val appInfo = "${buildConfig.applicationName}/${buildConfig.versionName}"
        val appIdInfo = "${buildConfig.applicationId}/${buildConfig.versionCode}"
        val androidInfo = "Android ${Build.VERSION.RELEASE}/${Build.VERSION.SDK_INT}"
        val deviceInfo = "${Build.MANUFACTURER}/${Build.MODEL}"
        return "$appInfo ($appIdInfo; $androidInfo; $deviceInfo) $transport"
    }
}

class DataSourceType(val factory: HttpDataSource.Factory, val transport: PlayerTransport)