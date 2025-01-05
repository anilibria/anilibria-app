package ru.radiationx.media.mobile

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.MediaItemTransition
import ru.radiationx.media.mobile.models.PlayerErrorState
import ru.radiationx.media.mobile.models.PlayerState
import ru.radiationx.media.mobile.models.PlaylistItem
import ru.radiationx.media.mobile.models.PlaylistState
import ru.radiationx.media.mobile.models.TimelineState
import ru.radiationx.media.mobile.models.asPlaybackState
import ru.radiationx.media.mobile.models.asTransitionReason
import ru.radiationx.media.mobile.models.toState

class PlayerFlow(
    private val coroutineScope: CoroutineScope,
) : PlayerAttachListener {

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            _playlistState.update { it.copy(currentItem = it.findByMediaItem(mediaItem)) }
            coroutineScope.launch {
                val transition = MediaItemTransition(mediaItem, reason.asTransitionReason())
                _mediaItemTransitionFlow.emit(transition)
            }
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            super.onAvailableCommandsChanged(availableCommands)
            _playerState.update { it.copy(commands = availableCommands.toState()) }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            val errorState = error?.let {
                val title = error.let { "${it.errorCode}, ${it.errorCodeName}" }
                val rootCause = error.findRootCause()
                val message = "${rootCause::class.simpleName}: ${rootCause.message}"
                PlayerErrorState(title, message)
            }
            _playerState.update { it.copy(error = errorState) }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            _playerState.update { it.copy(playWhenReady = playWhenReady) }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            _playerState.update { it.copy(isLoading = isLoading) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            _playerState.update { it.copy(playbackState = playbackState.asPlaybackState()) }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            if (videoSize == VideoSize.UNKNOWN) {
                return
            }
            _playerState.update { it.copy(videoSize = videoSize.toState()) }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            updateTimeline()
        }

        private fun Throwable.findRootCause(): Throwable {
            var rootCause: Throwable? = this
            while (rootCause?.cause != null && rootCause.cause !== rootCause) {
                rootCause = rootCause.cause
            }
            return rootCause ?: this
        }

    }

    private val _player = MutableStateFlow<PlayerProxy?>(null)

    private var timelineJob: Job? = null

    private val _playlistState = MutableStateFlow(PlaylistState())
    val playlistState = _playlistState.asStateFlow()

    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState = _timelineState.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _mediaItemTransitionFlow = MutableSharedFlow<MediaItemTransition>()
    val mediaItemTransitionFlow = _mediaItemTransitionFlow.asSharedFlow()

    override fun attachPlayer(player: PlayerProxy) {
        _player.value = player
        _playerState.update {
            PlayerState(
                playWhenReady = player.playWhenReady,
                isPlaying = player.isPlaying,
                isLoading = player.isLoading,
                playbackState = player.playbackState.asPlaybackState(),
                videoSize = player.videoSize.toState(),
                commands = player.availableCommands.toState()
            )
        }
        updateTimeline()
        player.addListener(playerListener)
        timelineJob?.cancel()
        timelineJob = coroutineScope.launch {
            while (true) {
                updateTimeline()
                delay(500)
            }
        }
    }

    override fun detachPlayer(player: PlayerProxy) {
        timelineJob?.cancel()
        player.removeListener(playerListener)
        _playerState.value = PlayerState()
        _timelineState.value = TimelineState()
        _player.value = null
    }

    fun prepare(
        playlist: List<PlaylistItem>,
        startIndex: Int? = null,
        startPosition: Long? = null,
    ) {
        withPlayer { player ->
            player.resetError()
            val currentItem = startIndex?.let { playlist[it] }
            _playlistState.update { PlaylistState(items = playlist, currentItem = currentItem) }
            player.setMediaItems(
                playlist.map { it.mediaItem },
                startIndex ?: C.INDEX_UNSET,
                startPosition ?: C.TIME_UNSET
            )
            player.prepare()
        }
    }

    fun changePlaylist(playlist: List<PlaylistItem>) {
        withPlayer { player ->
            player.resetError()
            _playlistState.update {
                PlaylistState(
                    items = playlist,
                    currentItem = playlist.getOrNull(player.currentMediaItemIndex)
                )
            }
            player.setMediaItems(
                playlist.map { it.mediaItem },
                false
            )
        }
    }

    fun play() {
        withPlayer {
            it.resetError()
            it.play()
        }
    }

    fun pause() {
        withPlayer {
            it.resetError()
            it.pause()
        }
    }

    fun prev() {
        withPlayer {
            it.resetError()
            it.seekToPreviousMediaItem()
        }
    }

    fun next() {
        withPlayer {
            it.resetError()
            it.seekToNextMediaItem()
        }
    }

    fun seekTo(position: Long) {
        withPlayer {
            it.resetError()
            it.seekTo(position)
        }
    }

    fun setSpeed(speed: Float) {
        withPlayer {
            it.resetError()
            it.setPlaybackSpeed(speed)
        }
    }

    private fun withPlayer(block: (PlayerProxy) -> Unit) {
        val player = _player.value
        if (player != null) {
            block.invoke(player)
        } else {
            withPlayerAsync(block)
        }
    }

    private fun withPlayerAsync(block: (PlayerProxy) -> Unit) {
        coroutineScope.launch {
            val player = _player.filterNotNull().first()
            block.invoke(player)
        }
    }

    private fun updateTimeline() {
        withPlayer { player ->
            _timelineState.value = TimelineState(
                duration = player.duration.coerceAtLeast(0),
                position = player.currentPosition.coerceAtLeast(0),
                bufferPosition = player.bufferedPosition.coerceAtLeast(0)
            )
        }
    }

    private fun PlayerProxy.resetError() {
        if (playerError != null) {
            prepare()
        }
    }
}

