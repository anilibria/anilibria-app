package ru.radiationx.data.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.di.PlayerClient
import ru.radiationx.data.network.UserAgentGenerator
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class PlayerDataSourceProvider @Inject constructor(
    private val context: Context,
    @PlayerClient private val okHttpClient: OkHttpClient,
    private val preferencesHolder: PreferencesHolder,
    private val buildConfig: SharedBuildConfig,
    private val userAgentGenerator: UserAgentGenerator
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
            setUserAgent(userAgentGenerator.systemTransport)
        }
        return DataSourceType(factory, PlayerTransport.SYSTEM)
    }

    @UnstableApi
    private fun createOkhttp(): DataSourceType {
        val factory = OkHttpDataSource.Factory(okHttpClient).apply {
            setUserAgent(userAgentGenerator.okHttpTransport)
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
            setUserAgent(userAgentGenerator.generate(cronet.versionString ?: "cronet/unknown"))
        }

        return DataSourceType(factory, PlayerTransport.CRONET)
    }

}

class DataSourceType(val factory: HttpDataSource.Factory, val transport: PlayerTransport)