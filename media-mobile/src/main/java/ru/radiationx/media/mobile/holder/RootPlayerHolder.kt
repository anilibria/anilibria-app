package ru.radiationx.media.mobile.holder

import androidx.media3.common.Player
import ru.radiationx.media.mobile.PlayerProxy

internal class RootPlayerHolder {

    private var _player: PlayerProxy? = null
    private val listeners = mutableListOf<PlayerAttachListener>()

    fun addListener(listener: PlayerAttachListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PlayerAttachListener) {
        listeners.remove(listener)
    }

    fun setPlayer(player: PlayerProxy?) {
        _player?.also { detachPlayer(it) }
        _player = player
        _player?.also { attachPlayer(it) }
    }

    private fun attachPlayer(player: PlayerProxy) {
        listeners.forEach { it.attachPlayer(player) }
    }

    private fun detachPlayer(player: PlayerProxy) {
        listeners.forEach { it.detachPlayer(player) }
    }

}