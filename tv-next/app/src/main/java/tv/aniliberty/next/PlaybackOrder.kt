package tv.aniliberty.next

/** Playback follows the server's ordered episode list, never ordinal + 1. */
fun nextEpisode(episodes: List<Episode>, currentId: String?): Episode? {
    val index = episodes.indexOfFirst { it.id == currentId }
    return if (index >= 0) episodes.getOrNull(index + 1) else null
}

/** Continue the saved episode, or the next server-ordered episode after completion. */
fun resumeEpisode(
    episodes: List<Episode>,
    lastId: String?,
    watchedPercent: Int = DEFAULT_WATCHED_PERCENT,
    progress: (String) -> Pair<Long, Long>,
): Episode? {
    val saved = episodes.firstOrNull { it.id == lastId } ?: return episodes.firstOrNull()
    val (position, duration) = progress(saved.id)
    return if (isEpisodeWatched(position, duration, watchedPercent)) nextEpisode(episodes, saved.id) ?: saved
    else saved
}
