package ru.radiationx.media.mobile.models

import androidx.media3.common.Player

data class PlayerCommandsState(
    val playPause: Boolean = false,
    val prepare: Boolean = false,
    val stop: Boolean = false,
    val seekToPreviousMediaItem: Boolean = false,
    val seekToPrevious: Boolean = false,
    val seekToNextMediaItem: Boolean = false,
    val seekToNext: Boolean = false,
    val seekToMediaItem: Boolean = false,
    val seekBack: Boolean = false,
    val seekForward: Boolean = false,
    val setSpeedAndPitch: Boolean = false,
)

fun Player.Commands.toState(): PlayerCommandsState = PlayerCommandsState(
    playPause = contains(Player.COMMAND_PLAY_PAUSE),
    prepare = contains(Player.COMMAND_PREPARE),
    stop = contains(Player.COMMAND_STOP),
    seekToPreviousMediaItem = contains(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM),
    seekToPrevious = contains(Player.COMMAND_SEEK_TO_PREVIOUS),
    seekToNextMediaItem = contains(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM),
    seekToNext = contains(Player.COMMAND_SEEK_TO_NEXT),
    seekToMediaItem = contains(Player.COMMAND_SEEK_TO_MEDIA_ITEM),
    seekBack = contains(Player.COMMAND_SEEK_BACK),
    seekForward = contains(Player.COMMAND_SEEK_FORWARD),
    setSpeedAndPitch = contains(Player.COMMAND_SET_SPEED_AND_PITCH)
)