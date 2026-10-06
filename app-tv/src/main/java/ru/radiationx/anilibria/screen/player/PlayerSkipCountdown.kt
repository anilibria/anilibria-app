package ru.radiationx.anilibria.screen.player

/** Counts only time spent playing after the prompt appears, with no focus on Watch. */
internal class PlayerSkipCountdown(private val durationMs: Long = 5_000) {
    var remainingMs = durationMs
        private set
    private var runningSince: Long? = null

    val remainingSeconds: Long get() = (remainingMs + 999) / 1_000
    val isFinished: Boolean get() = remainingMs == 0L

    fun reset() {
        remainingMs = durationMs
        runningSince = null
    }

    fun setRunning(running: Boolean, nowMs: Long) {
        runningSince?.let { previous ->
            remainingMs = (remainingMs - (nowMs - previous).coerceAtLeast(0)).coerceAtLeast(0)
        }
        runningSince = nowMs.takeIf { running && !isFinished }
    }
}
