package tv.aniliberty.next

/** In-memory titles only: bounded by entry count, character budget and age. */
internal class FranchisePreviewCache(
    private val maxEntries: Int = 32,
    private val maxCharacters: Int = 64_000,
    private val ttlMillis: Long = 15 * 60 * 1000L,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private data class Entry(val titles: List<String>, val saved: Long, val weight: Int)

    private val entries = LinkedHashMap<String, Entry>(16, .75f, true)
    val size: Int
        get() = entries.size

    val characters: Int
        get() = entries.values.sumOf { it.weight }

    operator fun get(id: String): List<String>? {
        val entry = entries[id] ?: return null
        if (now() - entry.saved >= ttlMillis) {
            entries.remove(id)
            return null
        }
        return entry.titles
    }

    fun containsKey(id: String) = get(id) != null

    operator fun set(id: String, titles: List<String>) {
        val weight = id.length + titles.sumOf { it.length }
        entries.remove(id)
        if (weight > maxCharacters) return
        entries[id] = Entry(titles.toList(), now(), weight)
        while (entries.size > maxEntries || characters > maxCharacters) entries.remove(
            entries.keys.first()
        )
    }
}
