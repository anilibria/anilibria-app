package ru.radiationx.media.mobile.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener

class SpeedController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
) : PlayerAttachListener {

    private val _state = MutableStateFlow(State())

    init {
        _state.onEach {
            val speed = if (it.longTap) {
                2f
            } else {
                it.speed
            }
            playerFlow.setSpeed(speed)
        }.launchIn(coroutineScope)
    }

    fun setLongTapSeek(state: Boolean) {
        _state.update { it.copy(longTap = state) }
    }

    fun setSpeed(speed: Float) {
        _state.update { it.copy(speed = speed) }
    }

    private data class State(
        val longTap: Boolean = false,
        val speed: Float = 1f,
    )
}