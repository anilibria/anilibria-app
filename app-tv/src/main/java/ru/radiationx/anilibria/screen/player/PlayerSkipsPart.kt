package ru.radiationx.anilibria.screen.player

import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.databinding.ViewPlayerSkipsBinding
import ru.radiationx.data.datasource.holders.AppPreference
import ru.radiationx.data.entity.domain.release.PlayerSkips
import timber.log.Timber

class PlayerSkipsPart(
    parent: FrameLayout,
    private val skipButtonText: String,
    private val coroutineScope: CoroutineScope,
    private val playerSkipsTimer: AppPreference<Boolean>,
    private val onSeek: (Long) -> Unit,
    private val onPlayPause: () -> Unit,
    private val onSkipShow: () -> Unit,
    private val onSkipHide: () -> Unit,
) {
    private val binding = ViewPlayerSkipsBinding.inflate(LayoutInflater.from(parent.context), parent, true)
    private val skipState = PlayerSkipState()
    private val playbackState = PlayerSkipPlaybackState()
    private val countdown = PlayerSkipCountdown()
    val isSeeking: Boolean get() = skipState.isSeeking
    private var activeSkip: PlayerSkips.Skip? = null
    private var isSkipVisible = false
    private var isHiding = false
    private var appearanceFinished = false
    private var autoSkipEnabled = false
    private var disposed = false
    private var timerJob: Job? = null
    private var lastTimerText: String? = null

    init {
        binding.root.isVisible = false
        binding.btSkipsSkip.text = skipButtonText
        binding.btSkipsCancel.setOnClickListener {
            if (!appearanceFinished) return@setOnClickListener
            dismissSkip()
            playerSkipsTimer.value = false
        }
        binding.btSkipsSkip.setOnClickListener {
            if (!appearanceFinished) return@setOnClickListener
            skip()
            playerSkipsTimer.value = true
        }
        binding.btSkipsCancel.setOnFocusChangeListener { _, focused ->
            trace("watchFocus=$focused")
            updateCountdown()
        }
        // These buttons are outside Leanback's grid, which normally routes media keys.
        listOf(binding.btSkipsSkip, binding.btSkipsCancel).forEach { button ->
            button.setOnKeyListener { _, keyCode, event ->
                if (keyCode != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) return@setOnKeyListener false
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onPlayPause()
                true
            }
        }
    }

    fun setSkips(skips: PlayerSkips?) {
        hidePrompt(immediate = true)
        resetTimer()
        activeSkip = null
        skipState.reset(skips)
        update(0)
    }

    fun onVideoLoading() {
        hidePrompt(immediate = true)
        playbackState.framePending()
        playbackState.update(isReady = false, isPlaying = false)
        resetTimer()
        activeSkip = null
        trace("videoLoading")
        update(skipState.currentPosition)
    }

    fun onFramePending(position: Long, positionChanged: Boolean) {
        playbackState.framePending(positionChanged)
        if (positionChanged) trace("framePending")
        update(position)
    }

    fun onFrameRendered(position: Long) {
        playbackState.frameRendered()
        trace("frameRendered")
        update(position)
    }

    fun onPlaybackChanged(isReady: Boolean, isPlaying: Boolean, position: Long) {
        if (playbackState.isReady != isReady || playbackState.isPlaying != isPlaying) {
            trace("playback ready=$isReady playing=$isPlaying")
        }
        playbackState.update(isReady, isPlaying)
        update(position)
    }

    fun onSeekStarted() {
        skipState.startSeek()
        update(skipState.currentPosition)
    }

    fun onSeekFinished(position: Long, cancelled: Boolean) {
        skipState.finishSeek(position, cancelled)
        update(position)
    }

    fun update(position: Long) {
        if (disposed) return
        skipState.update(position)
        val skip = skipState.currentSkip
        if (skip != activeSkip) {
            hidePrompt()
            resetTimer()
            activeSkip = skip
            if (skip != null) startTimer()
        }
        if (skip != null && playbackState.canShow) showPrompt() else hidePrompt()
        updateCountdown()
    }

    private fun showPrompt() {
        if (isSkipVisible) return
        binding.root.animate().withEndAction(null).cancel()
        isHiding = false
        isSkipVisible = true
        appearanceFinished = false
        if (!binding.root.isVisible) binding.root.alpha = 0f
        binding.root.isVisible = true
        setButtonsEnabled(true)
        onSkipShow()
        binding.btSkipsSkip.requestFocus()
        trace("appearanceStarted skipFocused=${binding.btSkipsSkip.hasFocus()}")
        binding.root.animate()
            .alpha(1f)
            .setDuration(250)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (disposed || !isSkipVisible) return@withEndAction
                appearanceFinished = true
                trace("appearanceFinished")
                updateCountdown()
            }
            .start()
    }

    private fun hidePrompt(immediate: Boolean = false) {
        if (!isSkipVisible && (!immediate || !isHiding)) return
        val notifyHide = isSkipVisible
        isSkipVisible = false
        appearanceFinished = false
        binding.root.animate().withEndAction(null).cancel()
        countdown.setRunning(false, SystemClock.uptimeMillis())
        isHiding = !immediate
        if (immediate) {
            binding.root.isVisible = false
            binding.root.alpha = 0f
            setButtonsEnabled(false)
            trace("hiddenImmediately")
        } else {
            // Clicks and the countdown are already blocked; keep styling/text during the fade.
            trace("disappearanceStarted")
            binding.root.animate()
                .alpha(0f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (disposed || !isHiding || isSkipVisible) return@withEndAction
                    isHiding = false
                    binding.root.isVisible = false
                    setButtonsEnabled(false)
                    trace("disappearanceFinished")
                }
                .start()
        }
        if (notifyHide) onSkipHide()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btSkipsSkip.isEnabled = enabled
        binding.btSkipsCancel.isEnabled = enabled
    }

    private fun dismissSkip() {
        skipState.dismissCurrentSkip()
        update(skipState.currentPosition)
    }

    private fun skip() {
        val skip = skipState.currentSkip ?: return
        dismissSkip()
        onSeek(skip.end)
    }

    private fun startTimer() {
        timerJob = coroutineScope.launch {
            autoSkipEnabled = withContext(Dispatchers.IO) { playerSkipsTimer.value }
            while (true) {
                updateCountdown()
                delay(100)
            }
        }
    }

    private fun updateCountdown() {
        val canCount = !disposed && isSkipVisible && autoSkipEnabled &&
            playbackState.canCount(appearanceFinished, binding.btSkipsCancel.hasFocus())
        countdown.setRunning(canCount, SystemClock.uptimeMillis())
        if (isHiding) return
        val text = if (autoSkipEnabled && isSkipVisible) {
            "$skipButtonText (${countdown.remainingSeconds})"
        } else skipButtonText
        if (text != lastTimerText) {
            binding.btSkipsSkip.text = text
            lastTimerText = text
            trace("timer seconds=${countdown.remainingSeconds} running=$canCount")
        }
        if (canCount && countdown.isFinished) skip()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        autoSkipEnabled = false
        countdown.reset()
        if (!isHiding) {
            binding.btSkipsSkip.text = skipButtonText
            lastTimerText = skipButtonText
        }
    }

    fun dispose() {
        disposed = true
        isHiding = false
        isSkipVisible = false
        resetTimer()
        binding.root.animate().withEndAction(null).cancel()
        binding.btSkipsCancel.onFocusChangeListener = null
        binding.btSkipsSkip.setOnKeyListener(null)
        binding.btSkipsCancel.setOnKeyListener(null)
        binding.root.isVisible = false
    }

    private fun trace(event: String) {
        if (BuildConfig.DEBUG) {
            Timber.tag("TvPlayerSkips").d("%s positionMs=%d remainingMs=%d", event, skipState.currentPosition, countdown.remainingMs)
        }
    }
}
