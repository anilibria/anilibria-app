package ru.radiationx.media.mobile.controllers

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.TimelineSkip

internal class SkipsController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val skipButtonCancel: View,
    private val skipButtonSkip: View,
) : PlayerAttachListener {

    private val _skipsData = MutableStateFlow(SkipsData())

    private val _currentSkip = MutableStateFlow<TimelineSkip?>(null)
    val currentSkip = _currentSkip.asStateFlow()

    init {
        skipButtonCancel.setOnClickListener {
            cancelCurrentSkip()
        }
        skipButtonSkip.setOnClickListener {
            _currentSkip.value?.also {
                playerFlow.seekTo(it.end)
            }
            cancelCurrentSkip()
        }

        combine(
            _skipsData,
            playerFlow.timelineState
        ) { skipsData, timelineState ->
            val skip = skipsData.skips.find {
                checkSkip(it, skipsData.skipped, timelineState.position)
            }
            _currentSkip.value = skip
        }.launchIn(coroutineScope)

        playerFlow.playlistState.map { it.currentItem }.distinctUntilChanged().onEach {
            _skipsData.value = SkipsData(it?.skips.orEmpty())
        }.launchIn(coroutineScope)
    }

    private fun checkSkip(skip: TimelineSkip, skipped: Set<TimelineSkip>, position: Long): Boolean {
        return skip !in skipped && (position in skip.start..skip.end)
    }

    private fun cancelCurrentSkip() {
        val skip = _currentSkip.value ?: return
        _skipsData.update {
            it.copy(skipped = it.skipped.plus(skip))
        }
    }

    private data class SkipsData(
        val skips: List<TimelineSkip> = emptyList(),
        val skipped: Set<TimelineSkip> = emptySet(),
    )
}