package ru.radiationx.media.mobile.holder

import androidx.media3.common.Player

internal interface PlayerAttachListener {
    fun attachPlayer(player: Player) {}
    fun detachPlayer(player: Player) {}
}