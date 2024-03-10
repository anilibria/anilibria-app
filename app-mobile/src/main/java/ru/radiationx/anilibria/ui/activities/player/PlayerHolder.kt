package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import ru.radiationx.data.di.providers.PlayerOkHttpProvider
import javax.inject.Inject

class PlayerHolder @Inject constructor(
    private val playerOkHttpProvider: PlayerOkHttpProvider,
) {

    private var _player: ExoPlayer? = null

    private var _mediaSession: MediaSession? = null

    fun init(context: Context) {
        val okHttpClient = playerOkHttpProvider.get()
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val dataSourceFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context).apply {
            setDataSourceFactory(dataSourceFactory)
        }
        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        val mediaSession = MediaSession.Builder(context, player).build()

        _mediaSession = mediaSession
        _player = player
    }

    fun destroy() {
        _mediaSession?.release()
        _mediaSession = null
        _player?.release()
        _player = null
    }

    fun getPlayer(): ExoPlayer {
        return requireNotNull(_player)
    }
}