package ru.radiationx.anilibria.screen.player

internal object EpisodeWatchPolicy {
    // Prefer the ending start; AniLiberty's web player supplies the 85% fallback.
    fun isViewed(
        position: Long,
        duration: Long,
        endingStart: Long? = null,
        ended: Boolean = false,
    ): Boolean {
        if (ended) return true
        if (position <= 0L) return false
        val validEndingStart = endingStart?.takeIf {
            it > 0L && (duration <= 0L || it < duration)
        }
        if (validEndingStart != null) return position >= validEndingStart
        return position > 0L && duration > 0L && position.toDouble() / duration >= 0.85
    }
}
