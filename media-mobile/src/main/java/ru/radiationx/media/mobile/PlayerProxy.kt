package ru.radiationx.media.mobile

import android.annotation.SuppressLint
import android.view.TextureView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener

class PlayerProxy {

    private var mediaItemsState = MediaItemsState()
    private var commonState = CommonState()

    private var _player: ExoPlayer? = null
    private val player: ExoPlayer
        get() = requireNotNull(_player)


    fun setPlayer(player: ExoPlayer) {
        _player = player
        restoreState()
    }

    fun removePlayer() {
        if (_player == null) {
            return
        }
        saveState()
        _player = null
    }

    fun destroy() {
        mediaItemsState = MediaItemsState()
        commonState = CommonState()
    }

    val playerError: ExoPlaybackException?
        get() = player.playerError

    val availableCommands: Player.Commands
        get() = player.availableCommands

    val videoSize: VideoSize
        get() = player.videoSize

    val playbackState: Int
        get() = player.playbackState

    val isLoading: Boolean
        get() = player.isLoading

    val isPlaying: Boolean
        get() = player.isPlaying

    val playWhenReady: Boolean
        get() = player.playWhenReady

    val currentMediaItemIndex: Int
        get() = player.currentMediaItemIndex

    val bufferedPosition: Long
        get() = player.bufferedPosition

    val currentPosition: Long
        get() = player.currentPosition

    val duration: Long
        get() = player.duration

    var pauseAtEndOfMediaItems: Boolean
        @SuppressLint("UnsafeOptInUsageError")
        get() = player.pauseAtEndOfMediaItems
        @SuppressLint("UnsafeOptInUsageError")
        set(value) {
            player.pauseAtEndOfMediaItems = value
        }

    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        player.setMediaItems(mediaItems, startIndex, startPositionMs)
    }

    fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
        player.setMediaItems(mediaItems, resetPosition)
    }

    fun prepare() {
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun seekToPreviousMediaItem() {
        player.seekToPreviousMediaItem()
    }

    fun seekToNextMediaItem() {
        player.seekToNextMediaItem()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun setVideoTextureView(textureView: TextureView) {
        player.setVideoTextureView(textureView)
    }

    fun clearVideoTextureView(textureView: TextureView) {
        player.clearVideoTextureView(textureView)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun saveState() {
        mediaItemsState.items = (0 until player.mediaItemCount).map {
            player.getMediaItemAt(it)
        }
        mediaItemsState.index = player.currentMediaItemIndex
        mediaItemsState.position = player.currentPosition

        commonState.playWhenReady = player.playWhenReady
        commonState.speed = player.playbackParameters.speed
        commonState.pauseAtEndOfMediaItems = player.pauseAtEndOfMediaItems
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun restoreState() {
        player.playWhenReady = commonState.playWhenReady
        player.pauseAtEndOfMediaItems = commonState.pauseAtEndOfMediaItems
        player.setMediaItems(mediaItemsState.items, mediaItemsState.index, mediaItemsState.position)
        player.prepare()
    }

    fun removeListener(playerListener: Player.Listener) {
        player.removeListener(playerListener)
    }

    fun addListener(playerListener: Player.Listener) {
        player.addListener(playerListener)
    }

    fun addAnalyticsListener(analyticsListener: AnalyticsListener) {
        player.addAnalyticsListener(analyticsListener)
    }

    fun removeAnalyticsListener(analyticsListener: AnalyticsListener) {
        player.removeAnalyticsListener(analyticsListener)
    }

    private class MediaItemsState(
        var items: List<MediaItem> = emptyList(),
        var index: Int = C.INDEX_UNSET,
        var position: Long = C.TIME_UNSET
    )

    private class CommonState(
        var playWhenReady: Boolean = false,
        var speed: Float = 1f,
        var pauseAtEndOfMediaItems: Boolean = false
    )
}