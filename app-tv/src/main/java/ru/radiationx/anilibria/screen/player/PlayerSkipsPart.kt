package ru.radiationx.anilibria.screen.player

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.databinding.ViewPlayerSkipsBinding
import ru.radiationx.data.datasource.holders.AppPreference
import ru.radiationx.data.entity.domain.release.PlayerSkips

/**
 * Обработчик пропуска опенингов
 *
 * @property parent корневой [android.view.ViewGroup]
 * @property skipButtonText текст для кнопки пропуска
 * @property coroutineScope скоуп для таймера
 * @property playerSkipsTimer включен ли таймер для автопропуска опенинга
 * @property onSeek функция для отлова намерения перемотки
 * @property onSkipShow функция для отлова события показа кнопки пропуска
 * @property onSkipHide функция для отлова события скрытия кнопки пропуска
 */
class PlayerSkipsPart(
    private val parent: FrameLayout,
    private val skipButtonText: String,
    private val coroutineScope: CoroutineScope,
    private val playerSkipsTimer: AppPreference<Boolean>,
    private val onSeek: (Long) -> Unit,
    private val onSkipShow: () -> Unit,
    private val onSkipHide: () -> Unit,
) {

    companion object {
        private const val TIMER_SEC = 5
    }

    private val binding = ViewPlayerSkipsBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        true
    )

    private val _timerFlow = MutableSharedFlow<Int?>(replay = 1)

    private var playerSkips: PlayerSkips? = null
    private val skippedList = mutableSetOf<PlayerSkips.Skip>()
    private var currentPosition = 0L
    private var isSkipVisible = false
    private var timerJob: Job? = null

    init {
        binding.btSkipsCancel.setOnClickListener {
            cancelSkip()
            onUserChoseWatchOpening()
        }
        binding.btSkipsSkip.setOnClickListener {
            skip()
            onUserChoseSkipOpening()
        }
        binding.root.isVisible = false
    }

    fun setSkips(skips: PlayerSkips?) {
        playerSkips = skips
        skippedList.clear()
    }

    /**
     * Вызывается периодически (например, playerGlue?.playbackListener?.onUpdateProgress())
     */
    fun update(position: Long) {
        currentPosition = position
        autoCancel()
        val skip = getCurrentSkip()
        val hasSkip = skip != null

        // Если раньше skip отображался, а сейчас нет - значит перепрыгнули
        if (skip == null && isSkipVisible) {
            isSkipVisible = false
            onSkipHide.invoke()
            binding.root.isVisible = false
        }

        // Если skip есть и кнопки не в фокусе — фокусируем по умолчанию на "Пропустить"
        if (hasSkip && (!binding.btSkipsSkip.isFocused && !binding.btSkipsCancel.isFocused)) {
            binding.btSkipsSkip.requestFocus()
        }

        if (hasSkip == isSkipVisible) return

        isSkipVisible = hasSkip
        if (hasSkip) {
            onSkipShow.invoke()
        } else {
            onSkipHide.invoke()
        }
        binding.root.isVisible = hasSkip
    }

    private fun getCurrentSkip(): PlayerSkips.Skip? {
        return playerSkips?.opening?.takeIf(::checkSkip)
            ?: playerSkips?.ending?.takeIf(::checkSkip)
    }

    private fun checkSkip(skip: PlayerSkips.Skip): Boolean {
        return !skippedList.contains(skip) &&
               currentPosition >= skip.start &&
               currentPosition <= skip.end
    }

    private fun autoCancel() {
        val opening = playerSkips?.opening
        val ending = playerSkips?.ending

        if (opening != null && opening !in skippedList && opening.end < currentPosition) {
            skippedList.add(opening)
        }
        if (ending != null && ending !in skippedList && ending.end < currentPosition) {
            skippedList.add(ending)
        }
    }

    private fun cancelSkip() {
        getCurrentSkip()?.also { skippedList.add(it) }
    }

    private suspend fun isAutoSkipEnabled(): Boolean = withContext(Dispatchers.IO) {
        playerSkipsTimer.value
    }

    private fun onUserChoseSkipOpening() {
        playerSkipsTimer.value = true
    }

    private fun onUserChoseWatchOpening() {
        playerSkipsTimer.value = false
    }

    private fun skip() {
        getCurrentSkip()?.also { onSeek(it.end) }
        cancelSkip()
    }

    private fun observeSkipTimerState() {
        _timerFlow
            .onEach { remainingTimeSec ->
                val text = remainingTimeSec?.let { "$skipButtonText ($it)" } ?: skipButtonText
                binding.btSkipsSkip.text = text
            }
            .launchIn(coroutineScope)
    }

    private fun startTimerIfNeed() {
        coroutineScope.launch {
            if (isAutoSkipEnabled()) {
                observeSkipTimerState()
                startTimer()
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = coroutineScope.launch {
            repeat(TIMER_SEC) { sec ->
                _timerFlow.emit(TIMER_SEC - sec)
                delay(1000)
            }
            _timerFlow.emit(null)
            skip()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerFlow.tryEmit(null)
    }
}
