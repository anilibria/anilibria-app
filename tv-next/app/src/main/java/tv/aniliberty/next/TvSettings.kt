package tv.aniliberty.next

import android.content.Context

class TvSettings(private val context: Context) {
    private val prefs = context.getSharedPreferences("tv-settings", Context.MODE_PRIVATE)

    var watchedPercent: Int
        get() = watchedPercent(playbackPreferences(context))
        set(value) {
            playbackPreferences(context).edit()
                .putInt(WATCHED_PERCENT_KEY, value.coerceIn(1, 100)).apply()
        }

    fun enabled(key: String, default: Boolean = true) = prefs.getBoolean(key, default)

    fun toggle(key: String, default: Boolean = true): Boolean =
        (!enabled(key, default)).also { prefs.edit().putBoolean(key, it).apply() }
}

data class EpisodeSkip(val start: Double, val stop: Double)

internal fun skipTarget(range: EpisodeSkip?, positionMs: Long, durationMs: Long): Long? {
    if (
        range == null ||
            !range.start.isFinite() ||
            !range.stop.isFinite() ||
            range.start < 0 ||
            range.stop <= range.start
    )
        return null
    val start = (range.start * 1000).toLong()
    val end = (range.stop * 1000).toLong()
    if (durationMs <= 0 || end > durationMs || positionMs < start || positionMs >= end) return null
    return end
}

internal const val WATCHED_PERCENT_KEY = "watched-percent"
internal const val DEFAULT_WATCHED_PERCENT = 90

internal fun watchedPercent(prefs: android.content.SharedPreferences): Int =
    prefs.getInt(WATCHED_PERCENT_KEY, DEFAULT_WATCHED_PERCENT).coerceIn(1, 100)

internal fun isEpisodeWatched(position: Long, duration: Long, percent: Int = DEFAULT_WATCHED_PERCENT): Boolean =
    position > 0 && duration > 0 &&
        position.toDouble() / duration >= percent.coerceIn(1, 100) / 100.0

internal fun showWatchedPercent(context: Context, changed: () -> Unit) {
    val settings = TvSettings(context)
    val panel = TvSidePanel(context, "Считать серию просмотренной после")
    var selected: android.view.View? = null
    (50..100 step 5).forEach { percent ->
        val button = panel.addChoice("$percent%", selected = percent == settings.watchedPercent) {
            settings.watchedPercent = percent
            panel.dismiss()
            changed()
        }
        if (percent == settings.watchedPercent) selected = button
    }
    panel.show()
    selected?.requestFocus()
}
