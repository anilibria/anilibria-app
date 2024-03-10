package ru.radiationx.media.mobile.holder

import androidx.media3.common.Player

internal class RootPlayerHolder {

    private var _player: Player? = null
    private val listeners = mutableListOf<PlayerAttachListener>()

    fun addListener(listener: PlayerAttachListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PlayerAttachListener) {
        listeners.remove(listener)
    }

    fun setPlayer(player: Player?) {
        _player?.also { detachPlayer(it) }
        _player = player
        _player?.also { attachPlayer(it) }
    }

    private fun attachPlayer(player: Player) {
        listeners.forEach { it.attachPlayer(player) }
    }

    private fun detachPlayer(player: Player) {
        listeners.forEach { it.detachPlayer(player) }
    }

}