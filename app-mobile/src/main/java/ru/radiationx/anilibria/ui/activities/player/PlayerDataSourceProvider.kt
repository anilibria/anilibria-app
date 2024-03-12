package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import org.chromium.net.CronetEngine
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
        return DataSourceType.System(
            DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        )
    }

    private fun createOkhttp(): DataSourceType {
        return DataSourceType.OkHttp(OkHttpDataSource.Factory(playerOkHttpProvider.get()))
    }

    private fun createCronet(): DataSourceType? {
        val cronet = try {
            CronetEngine.Builder(context)
                .enableHttp2(true)
                .build()
        } catch (ex: Throwable) {
            Timber.e(ex, "tryCronet")
            return null
        }

        return DataSourceType.Cronet(
            cronet.versionString ?: "unknown",
            CronetDataSource.Factory(cronet, cronetThreadPool)
        )
    }

}

sealed class DataSourceType(val factory: DataSource.Factory, val transport: PlayerTransport) {

    class System(
        factory: DefaultHttpDataSource.Factory,
    ) : DataSourceType(factory, PlayerTransport.SYSTEM)

    class OkHttp(
        factory: OkHttpDataSource.Factory,
    ) : DataSourceType(factory, PlayerTransport.OKHTTP)

    class Cronet(
        val version: String,
        factory: CronetDataSource.Factory,
    ) : DataSourceType(factory, PlayerTransport.CRONET)
}