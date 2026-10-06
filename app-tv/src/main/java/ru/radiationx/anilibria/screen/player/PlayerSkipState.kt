package ru.radiationx.anilibria.screen.player

import ru.radiationx.data.entity.domain.release.PlayerSkips

internal class PlayerSkipState {
    private var skips: PlayerSkips? = null
    private val dismissed = mutableSetOf<PlayerSkips.Skip>()
    var currentPosition = 0L
        private set
    var isSeeking = false
        private set

    val currentSkip: PlayerSkips.Skip?
        get() = if (isSeeking) null else
            skips?.opening?.takeIf(::isCurrent) ?: skips?.ending?.takeIf(::isCurrent)

    fun reset(skips: PlayerSkips?) {
        this.skips = skips
        dismissed.clear()
        currentPosition = 0L
        isSeeking = false
    }

    fun update(position: Long) {
        currentPosition = position
        // Preview positions must not mark openings/endings as watched when a seek is cancelled.
        if (isSeeking) return
        skips?.opening?.takeIf { it.end < position }?.let(dismissed::add)
        skips?.ending?.takeIf { it.end < position }?.let(dismissed::add)
    }

    fun startSeek() {
        isSeeking = true
    }

    fun finishSeek(position: Long, cancelled: Boolean) {
        currentPosition = position
        isSeeking = false
        // A confirmed seek can return to a range already watched or dismissed.
        if (!cancelled) dismissed.removeAll { position in it.start..it.end }
        update(position)
    }

    fun dismissCurrentSkip() {
        currentSkip?.let(dismissed::add)
    }

    private fun isCurrent(skip: PlayerSkips.Skip): Boolean =
        skip !in dismissed && currentPosition in skip.start..skip.end
}
