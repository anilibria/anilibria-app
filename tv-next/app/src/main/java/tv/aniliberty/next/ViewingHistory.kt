package tv.aniliberty.next

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ContinueEpisode(
    val releaseId: Int,
    val episodeId: String,
    val releaseTitle: String,
    val ordinal: Double,
    val episodeName: String,
    val preview: String?,
    val position: Long,
    val duration: Long,
    val updatedAt: Long,
    val nextUp: Boolean = false,
    val watchedPercent: Int = DEFAULT_WATCHED_PERCENT,
) {
    val episodeTitle: String
        get() =
            listOf("Эпизод ${number(ordinal)}", episodeName)
                .filter(String::isNotBlank)
                .joinToString(" · ")

    val remaining: Long
        get() = (duration - position).coerceAtLeast(0)

    val completed: Boolean
        get() = isEpisodeWatched(position, duration, watchedPercent)
}

private data class PlaybackRecord(val current: ContinueEpisode, val following: ContinueEpisode?)

/** Metadata only; HLS URLs and authentication data never go into the home history. */
class ViewingHistory(private val prefs: SharedPreferences) {
    private fun decode(item: JSONObject) =
        ContinueEpisode(
            item.getInt("release"),
            item.getString("episode"),
            item.getString("title"),
            item.getDouble("ordinal"),
            item.optString("name"),
            item.optString("preview").takeIf { it.isNotBlank() },
            item.getLong("position"),
            item.getLong("duration"),
            item.optLong("updated"),
            item.optBoolean("nextUp"),
            watchedPercent(prefs),
        )

    private fun encode(item: ContinueEpisode) =
        JSONObject()
            .put("release", item.releaseId)
            .put("episode", item.episodeId)
            .put("title", item.releaseTitle)
            .put("ordinal", item.ordinal)
            .put("name", item.episodeName)
            .put("preview", item.preview.orEmpty())
            .put("position", item.position)
            .put("duration", item.duration)
            .put("updated", item.updatedAt)
            .put("nextUp", item.nextUp)

    private fun records(): List<PlaybackRecord> =
        runCatching {
                val array = JSONArray(prefs.getString("continue-episodes", "[]"))
                (0 until array.length()).mapNotNull { index ->
                    runCatching {
                            val item = array.getJSONObject(index)
                            PlaybackRecord(decode(item), item.optJSONObject("next")?.let(::decode))
                        }
                        .getOrNull()
                }
            }
            .getOrDefault(emptyList())

    fun items(): List<ContinueEpisode> =
        records()
            .groupBy { it.current.releaseId }
            .values
            .mapNotNull { episodes ->
                val record = episodes.maxByOrNull { it.current.updatedAt } ?: return@mapNotNull null
                if (
                    prefs.contains("continue-hidden:${record.current.releaseId}") &&
                        record.current.updatedAt <=
                            prefs.getLong("continue-hidden:${record.current.releaseId}", 0)
                )
                    return@mapNotNull null
                when {
                    record.current.position > 0 && !record.current.completed -> record.current
                    record.current.completed -> record.following?.takeIf { !it.completed }
                    record.current.position == 0L &&
                        episodes.any {
                            it.current.completed &&
                                it.following?.episodeId == record.current.episodeId
                        } -> record.current.copy(nextUp = true)
                    else -> null
                }
            }
            .sortedByDescending { it.updatedAt }

    fun dismiss(releaseId: Int) {
        prefs
            .edit()
            .putLong("continue-hidden:$releaseId", System.currentTimeMillis())
            .putStringSet(
                "continue-hidden-episodes:$releaseId",
                prefs.getStringSet("continue-available:$releaseId", emptySet()),
            )
            .apply()
    }

    fun restore(releaseId: Int) {
        prefs
            .edit()
            .remove("continue-hidden:$releaseId")
            .remove("continue-hidden-episodes:$releaseId")
            .apply()
    }

    fun hiddenReleaseIds(): List<Int> =
        prefs.all.keys
            .filter { it.startsWith("continue-hidden:") }
            .mapNotNull { it.substringAfter(":").toIntOrNull() }

    fun knownReleaseIds(): Set<Int> = records().map { it.current.releaseId }.toSet()

    fun latestWatched(): List<ContinueEpisode> =
        records()
            .groupBy { it.current.releaseId }
            .values
            .mapNotNull { it.maxByOrNull { r -> r.current.updatedAt }?.current }
            .sortedByDescending { it.updatedAt }

    fun save(
        release: Release,
        episode: Episode,
        position: Long,
        duration: Long,
        updatedAt: Long = System.currentTimeMillis(),
    ) {
        val available = release.episodes.filter { it.streams.isNotEmpty() }.map { it.id }.toSet()
        val hiddenEpisodes = prefs.getStringSet("continue-hidden-episodes:${release.id}", null)
        if (hiddenEpisodes != null && available.any { it !in hiddenEpisodes }) restore(release.id)
        prefs.edit().putStringSet("continue-available:${release.id}", available).apply()
        val entry =
            ContinueEpisode(
                release.id,
                episode.id,
                release.title,
                episode.ordinal,
                episode.name,
                episode.preview,
                position.coerceAtLeast(0),
                duration.coerceAtLeast(0),
                updatedAt,
                watchedPercent = watchedPercent(prefs),
            )
        val index = release.episodes.indexOfFirst { it.id == episode.id }
        val next =
            if (index >= 0 && !release.blocked)
                release.episodes.drop(index + 1).firstOrNull { e ->
                    val saved = prefs.getLong("position:${e.id}", 0)
                    val length = prefs.getLong("duration:${e.id}", e.duration * 1000)
                    e.streams.isNotEmpty() && !isEpisodeWatched(saved, length, watchedPercent(prefs))
                }
            else null
        val following =
            next?.let { e ->
                val saved = prefs.getLong("position:${e.id}", 0)
                ContinueEpisode(
                    release.id,
                    e.id,
                    release.title,
                    e.ordinal,
                    e.name,
                    e.preview,
                    saved,
                    prefs.getLong("duration:${e.id}", e.duration * 1000),
                    updatedAt,
                    nextUp = saved == 0L,
                    watchedPercent = watchedPercent(prefs),
                )
            }
        val updated = PlaybackRecord(entry, following)
        val records =
            (listOf(updated) + records().filter { it.current.episodeId != episode.id })
                .sortedByDescending { it.current.updatedAt }
                .take(60)
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                encode(record.current).apply { record.following?.let { put("next", encode(it)) } }
            )
        }
        prefs.edit().putString("continue-episodes", array.toString()).apply()
    }
}

fun homeSectionHeader(rows: List<Row>, shelfIndex: Int): Int? =
    (shelfIndex downTo 0).firstOrNull { index ->
        rows.getOrNull(index)?.kind in setOf(Kind.HEADING, Kind.DAYS)
    }
