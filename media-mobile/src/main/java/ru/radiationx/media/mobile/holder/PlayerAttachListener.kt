package ru.radiationx.media.mobile.holder

import ru.radiationx.media.mobile.PlayerProxy

internal interface PlayerAttachListener {
    fun attachPlayer(player: PlayerProxy) {}
    fun detachPlayer(player: PlayerProxy) {}
}