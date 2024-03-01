package ru.radiationx.anilibria.ui.activities.player.controllers

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class KeepScreenOnController(
    private val activity: ComponentActivity,
) {

    companion object {
        private val timerThreshold = TimeUnit.HOURS.toMillis(1)
        private val checkInterval = TimeUnit.SECONDS.toMillis(30)
    }

    private val _config = MutableStateFlow(Config())
    private val _state = MutableStateFlow(false)

    val state = _state.asStateFlow()

    init {
        activity.lifecycleScope.launch {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    updateState()
                    delay(checkInterval)
                }
            }
        }

        _config.onEach {
            updateState()
        }.launchIn(activity.lifecycleScope)
    }

    fun setTimerEnabled(state: Boolean) {
        _config.update { it.copy(timerEnabled = state) }
    }

    fun setPlaying(state: Boolean) {
        _config.update { it.copy(isPlaying = state) }
    }

    fun onUserInteraction() {
        _config.update { it.copy(lastInteractionTime = SystemClock.elapsedRealtime()) }
    }

    private fun updateState() {
        _state.value = _config.value.let { it.isPlaying && it.isActiveByTimer() }
    }

    private fun Config.isActiveByTimer(): Boolean {
        if (!timerEnabled) return true
        val delta = SystemClock.elapsedRealtime() - lastInteractionTime
        return delta < timerThreshold
    }

    private data class Config(
        val isPlaying: Boolean = false,
        val timerEnabled: Boolean = false,
        val lastInteractionTime: Long = SystemClock.elapsedRealtime(),
    )
}