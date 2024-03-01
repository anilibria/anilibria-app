package ru.radiationx.media.mobile.controllers

import android.util.Log
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.TimelineSkip

internal class SkipsController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val skipButtonCancel: MaterialButton,
    private val skipButtonSkip: MaterialButton,
) : PlayerAttachListener {

    companion object {
        private const val TIMER_SEC = 5
    }

    private val _skipsData = MutableStateFlow(SkipsData())

    private var timerJob: Job? = null
    private val _timerState = MutableStateFlow<Int?>(null)
    private val _timerEnabled = MutableStateFlow(true)

    private val _currentSkip = MutableStateFlow<TimelineSkip?>(null)
    val currentSkip = _currentSkip.asStateFlow()

    init {
        skipButtonCancel.setOnClickListener {
            cancelCurrentSkip()
        }
        skipButtonSkip.setOnClickListener {
            skip()
        }

        combine(
            _skipsData,
            playerFlow.timelineState.filter { it.duration > 0 },
            playerFlow.playerState.map { it.isBlockingLoading }.distinctUntilChanged()
        ) { skipsData, timelineState, isBlockingLoading ->
            val skip = skipsData.skips.find {
                checkSkip(it, skipsData.skipped, timelineState.position)
            }
            _currentSkip.value = skip?.takeIf { !isBlockingLoading }
        }.launchIn(coroutineScope)

        playerFlow.playlistState.map { it.currentItem }.distinctUntilChanged().onEach {
            _skipsData.value = SkipsData(it?.skips.orEmpty())
        }.launchIn(coroutineScope)

        combine(
            _currentSkip,
            _timerEnabled
        ) { skip, enabled ->
            Log.e("kekeke", "active skip $skip; $enabled")
            if (enabled && skip != null) {
                startTimer()
            } else {
                stopTimer()
            }
        }.launchIn(coroutineScope)

        _timerState.onEach {
            Log.e("kekeke", "active skip timer $it")
            val text = if (it != null) {
                "Пропустить ($it)"
            } else {
                "Пропустить"
            }
            skipButtonSkip.text = text
        }.launchIn(coroutineScope)
    }

    fun setTimerEnabled(state: Boolean) {
        _timerEnabled.value = state
    }

    private fun checkSkip(skip: TimelineSkip, skipped: Set<TimelineSkip>, position: Long): Boolean {
        return skip !in skipped && (position in skip.start..skip.end)
    }

    private fun skip() {
        _currentSkip.value?.also {
            playerFlow.seekTo(it.end)
        }
        cancelCurrentSkip()
    }

    private fun cancelCurrentSkip() {
        val skip = _currentSkip.value ?: return
        _skipsData.update {
            it.copy(skipped = it.skipped.plus(skip))
        }
    }

    private fun startTimer() {
        stopTimer()
        val job = coroutineScope.launch {
            repeat(TIMER_SEC) { sec ->
                _timerState.value = TIMER_SEC - sec
                playerFlow.playerState.filter { it.playWhenReady }.first()
                delay(1000)
            }
            _timerState.value = null
            skip()
        }
        timerJob = job
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = null
    }

    private data class SkipsData(
        val skips: List<TimelineSkip> = emptyList(),
        val skipped: Set<TimelineSkip> = emptySet(),
    )
}