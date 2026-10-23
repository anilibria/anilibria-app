package tv.aniliberty.next

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Guest data belongs to this TV and never changes when an account signs in or out. */
class LocalLibrary(context: Context) {
    private val prefs = context.getSharedPreferences("viewing", Context.MODE_PRIVATE)

    private fun saved(): List<JSONObject> {
        val array =
            runCatching { JSONArray(prefs.getString("library", "[]")) }.getOrDefault(JSONArray())
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }

    fun lists(): AccountLists {
        val favorites =
            saved()
                .filter { it.optBoolean("favorite", true) }
                .map { it.optInt("id") }
                .filter { it > 0 }
                .toSet()
        val collections =
            runCatching { parseCollectionIds(prefs.getString("local-collections", "[]")!!) }
                .getOrDefault(emptyMap())
        return AccountLists(favorites, collections)
    }

    fun remember(release: Release) {
        val previous = saved()
        val item =
            JSONObject()
                .put("id", release.id)
                .put("favorite", release.id in lists().favorites)
                .put("title", release.title)
                .put(
                    "name",
                    JSONObject().put("main", release.title).put("english", release.english),
                )
                .put("poster", JSONObject().put("preview", release.poster.orEmpty()))
                .put("year", release.year)
                .put("season", JSONObject().put("description", release.season))
                .put("type", JSONObject().put("description", release.type))
                .put("age_rating", JSONObject().put("description", release.age))
                .put(
                    "genres",
                    JSONArray().apply {
                        release.genres.forEach { put(JSONObject().put("name", it)) }
                    },
                )
        write(listOf(item) + previous.filter { it.optInt("id") != release.id })
    }

    fun favorite(id: Int, added: Boolean) {
        write(saved().map { if (it.optInt("id") == id) it.put("favorite", added) else it })
    }

    fun collection(id: Int, type: CollectionType?) {
        val values = lists().collections.toMutableMap()
        if (type == null) values.remove(id) else values[id] = type
        val json = JSONArray()
        values.forEach { (key, value) -> json.put(JSONArray().put(key).put(value.name)) }
        prefs.edit().putString("local-collections", json.toString()).apply()
    }

    private fun write(items: List<JSONObject>) {
        val array = JSONArray()
        items.forEach { array.put(it) }
        prefs.edit().putString("library", array.toString()).apply()
    }

    fun release(id: Int, title: String = "Тайтл $id"): Release =
        saved()
            .firstOrNull { it.optInt("id") == id }
            ?.let {
                parseRelease(
                    JSONObject(it.toString()).apply {
                        if (!has("name"))
                            put("name", JSONObject().put("main", optString("title", title)))
                    }
                )
            }
            ?: parseRelease(JSONObject().put("id", id).put("name", JSONObject().put("main", title)))

    fun releases(screen: Screen): List<Release> {
        val lists = lists()
        val ids =
            when (screen) {
                is Screen.Favorites -> lists.favorites
                is Screen.Collections -> lists.collections.filterValues { it == screen.type }.keys
                else -> emptySet()
            }
        val filter = accountListFilter(screen)
        val query = accountListQuery(screen).trim()
        val items =
            ids.map { release(it) }
                .filter {
                    (query.isBlank() ||
                        it.title.contains(query, true) ||
                        it.english.contains(query, true)) &&
                        (filter.years.isEmpty() || it.year in filter.years) &&
                        (filter.types.isEmpty() || it.type in filter.types) &&
                        (filter.ages.isEmpty() || it.age in filter.ages) &&
                        (filter.genres.isEmpty() ||
                            it.genres.any { genre -> genre in filter.genres })
                }
        return when (filter.sorting) {
            "NAME_ASC" -> items.sortedBy { it.title.lowercase() }
            "YEAR_DESC" -> items.sortedByDescending { it.year }
            "YEAR_ASC" -> items.sortedBy { it.year }
            else -> items
        }
    }

    fun references(favorites: Boolean): Map<String, List<CatalogChoice>> {
        val items =
            releases(if (favorites) Screen.Favorites() else Screen.Collections()) +
                lists().collections.keys.map { release(it) }
        fun choices(values: List<String>) =
            values.filter(String::isNotBlank).distinct().sorted().map { CatalogChoice(it, it) }
        return mapOf(
            "genres" to choices(items.flatMap { it.genres }),
            "types" to choices(items.map { it.type }),
            "years" to choices(items.map { it.year }),
            "age-ratings" to choices(items.map { it.age }),
            "sorting" to
                listOf(
                    CatalogChoice("CREATED_AT_DESC", "Сначала добавленные"),
                    CatalogChoice("NAME_ASC", "По названию"),
                    CatalogChoice("YEAR_DESC", "Сначала новые"),
                    CatalogChoice("YEAR_ASC", "Сначала старые"),
                ),
        )
    }

    fun history(): AccountHistoryPage {
        val items =
            ViewingHistory(prefs).latestWatched().map { record ->
                val release = release(record.releaseId, record.releaseTitle)
                val episode =
                    Episode(
                        record.episodeId,
                        record.ordinal,
                        record.episodeName,
                        record.preview,
                        record.duration / 1000,
                        emptyList(),
                        record.ordinal,
                    )
                AccountViewedEpisode(
                    release,
                    episode,
                    AccountTimecode(record.episodeId, record.position / 1000.0, record.completed),
                    java.time.Instant.ofEpochMilli(record.updatedAt).toString(),
                )
            }
        return AccountHistoryPage(items, false)
    }
}
