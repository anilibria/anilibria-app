package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import ru.radiationx.data.entity.common.PlayerTransport
import java.util.UUID
import javax.inject.Inject

class PlayerHolder @Inject constructor(
    private val dataSourceProvider: PlayerDataSourceProvider,
) {

    private var _player: ExoPlayer? = null

    private var _mediaSession: MediaSession? = null

    var selectedTransport: PlayerTransport? = null
        private set

    @UnstableApi
    fun init(context: Context) {
        val dataSourceType = dataSourceProvider.get()
        selectedTransport = dataSourceType.transport
        val dataSourceFactory = DefaultDataSource.Factory(context, dataSourceType.factory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context).apply {
            setDataSourceFactory(dataSourceFactory)
        }
        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        val sessionId = UUID.randomUUID().toString()
        val mediaSession = MediaSession.Builder(context, player).setId(sessionId).build()

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