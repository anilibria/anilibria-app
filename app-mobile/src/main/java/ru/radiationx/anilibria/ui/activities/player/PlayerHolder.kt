package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import ru.radiationx.data.player.PlayerCacheDataSourceProvider
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.data.player.PlayerTransport
import ru.radiationx.media.mobile.PlayerProxy
import java.util.UUID
import javax.inject.Inject

class PlayerHolder @Inject constructor(
    private val dataSourceProvider: PlayerDataSourceProvider,
    private val cacheDataSourceProvider: PlayerCacheDataSourceProvider
) {

    private val playerProxy = PlayerProxy()
    private var _player: ExoPlayer? = null

    private var _mediaSession: MediaSession? = null

    var selectedTransport: PlayerTransport? = null
        private set

    @UnstableApi
    fun attach(context: Context) {
        val dataSourceType = dataSourceProvider.get()
        selectedTransport = dataSourceType.transport
        val dataSourceFactory = DefaultDataSource.Factory(context, dataSourceType.factory)
        val cacheFactory = cacheDataSourceProvider.createCacheFactory(dataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context).apply {
            setDataSourceFactory(cacheFactory)
        }
        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build()

        _player = player
        startMediaSession(context, player)
        playerProxy.setPlayer(player)
    }

    fun detach() {
        playerProxy.removePlayer()
        stopMediaSession()
        _player?.release()
        _player = null
        selectedTransport = null
    }

    fun destroy() {
        detach()
        playerProxy.destroy()
    }

    fun getPlayer(): PlayerProxy {
        requireNotNull(_player)
        return playerProxy
    }

    private fun startMediaSession(context: Context, player: ExoPlayer) {
        stopMediaSession()
        val sessionId = UUID.randomUUID().toString()
        val mediaSession = MediaSession.Builder(context, player).setId(sessionId).build()
        _mediaSession = mediaSession
    }

    private fun stopMediaSession() {
        _mediaSession?.release()
        _mediaSession = null
    }
}