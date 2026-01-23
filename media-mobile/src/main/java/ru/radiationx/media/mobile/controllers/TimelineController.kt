package ru.radiationx.media.mobile.controllers

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import java.util.concurrent.TimeUnit
import java.util.Locale
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
        val hours = TimeUnit.MILLISECONDS.toHours(seek ?: position)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(seek ?: position) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(seek ?: position) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}