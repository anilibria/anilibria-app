package ru.radiationx.media.mobile.controllers

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.models.TimelineState
import ru.radiationx.media.mobile.utils.TimeFormatter

internal class TimelineController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val mediaSeekBar: AppCompatSeekBar,
    private val mediaTime: TextView,
) : PlayerAttachListener {

    private val _seekState = MutableStateFlow<Int?>(null)
    val seekState = _seekState.asStateFlow()

    init {
        mediaSeekBar.max = 100

        mediaSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                _seekState.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                _seekState.value = seekBar.progress
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                _seekState.value = null
                playerFlow.seekTo(seekBar.progress.toLong())
            }
        })

        val timelineState = playerFlow.timelineState

        timelineState.map { it.duration }.distinctUntilChanged().onEach {
            if (it > 0L) {
                mediaSeekBar.max = it.toInt()
            }
        }.launchIn(coroutineScope)

        timelineState.onEach {
            mediaSeekBar.secondaryProgress = it.bufferPosition.coerceIn(0, it.duration).toInt()
        }.launchIn(coroutineScope)

        combine(timelineState, seekState) { timeline, seek ->
            if (seek == null) {
                mediaSeekBar.progress = timeline.position.coerceIn(0, timeline.duration).toInt()
            }
            mediaTime.text = timeline.formatTime(seek?.toLong())
        }.launchIn(coroutineScope)
    }

    private fun TimelineState.formatTime(seek: Long?): String {
        return "${TimeFormatter.format(seek ?: position)} / ${TimeFormatter.format(duration)}"
    }
}