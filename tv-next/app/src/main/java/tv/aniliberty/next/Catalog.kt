package tv.aniliberty.next

import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

data class CatalogFilter(
    val genres: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val seasons: Set<String> = emptySet(),
    val ages: Set<String> = emptySet(),
    val publish: Set<String> = emptySet(),
    val production: Set<String> = emptySet(),
    val fromYear: String = "",
    val toYear: String = "",
    val sorting: String = "FRESH_AT_DESC",
) {
    val count: Int
        get() =
            listOf(genres, types, seasons, ages, publish, production).sumOf { it.size } +
                (if (fromYear.isNotBlank() || toYear.isNotBlank()) 1 else 0)
}

data class CatalogChoice(val value: String, val title: String)

data class CatalogPage(val releases: List<Release>, val more: Boolean, val total: Int?)

val catalogReferencePaths =
    linkedMapOf(
        "genres" to "Жанры",
        "types" to "Формат",
        "publish-statuses" to "Статус выхода",
        "production-statuses" to "Статус озвучки",
        "seasons" to "Сезон",
        "years" to "Годы",
        "age-ratings" to "Возраст",
        "sorting" to "Сортировка",
    )

fun catalogPath(screen: Screen.Catalog, page: Int): String {
    require(page > 0)
    val f = screen.filters
    require(f.fromYear.isBlank() || f.toYear.isBlank() || f.fromYear.toInt() <= f.toYear.toInt()) {
        "Начальный год больше конечного"
    }
    val args = linkedMapOf("limit" to "30", "page" to page.toString(), "f[sorting]" to f.sorting)
    if (screen.query.isNotBlank()) args["f[search]"] = screen.query.trim()
    val genres = f.genres + listOfNotNull(screen.genre?.toString())
    listOf(
            "genres" to genres,
            "types" to f.types,
            "seasons" to f.seasons,
            "age_ratings" to f.ages,
            "publish_statuses" to f.publish,
            "production_statuses" to f.production,
        )
        .forEach { (key, values) ->
            if (values.isNotEmpty()) args["f[$key]"] = values.sorted().joinToString(",")
        }
    if (f.fromYear.isNotBlank()) args["f[years][from_year]"] = f.fromYear
    if (f.toYear.isNotBlank()) args["f[years][to_year]"] = f.toYear
    return "/anime/catalog/releases?" +
        args.entries.joinToString("&") {
            URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8")
        }
}

fun parseCatalogChoices(body: String, years: Boolean = false): List<CatalogChoice> {
    val array = JSONArray(body)
    return (0 until array.length())
        .mapNotNull { i ->
            if (years)
                array
                    .optString(i)
                    .takeIf { it.toIntOrNull() != null }
                    ?.let { CatalogChoice(it, it) }
            else
                array.optJSONObject(i)?.let { item ->
                    val value =
                        if (item.has("value")) item.optString("value") else item.optString("id")
                    val label =
                        listOf("label", "name", "description").firstNotNullOfOrNull { key ->
                            item.optString(key).takeIf { it.isNotBlank() && it != "null" }
                        }
                    if (value.isBlank() || value == "null" || label == null) null
                    else CatalogChoice(value, label)
                }
        }
        .distinctBy { it.value }
        .let { if (years) it.sortedByDescending { c -> c.value.toInt() } else it }
}

fun parseCatalogPage(body: String, page: Int): CatalogPage {
    val json = JSONObject(body)
    val data = json.optJSONArray("data") ?: JSONArray()
    val releases =
        (0 until data.length())
            .mapNotNull { data.optJSONObject(it)?.let(::parseRelease) }
            .distinctBy { it.id }
    val rootMeta = json.optJSONObject("meta")
    val pagination = rootMeta?.optJSONObject("pagination") ?: rootMeta
    val total = pagination?.optInt("total", -1)?.takeIf { it >= 0 }
    val current = pagination?.optInt("current_page", page) ?: page
    val pages = pagination?.optInt("total_pages", pagination.optInt("last_page", -1)) ?: -1
    val hasNext =
        if (pages >= 0) current < pages
        else
            pagination?.optJSONObject("links")?.optString("next")?.let {
                it.isNotBlank() && it != "null"
            } == true
    return CatalogPage(releases, releases.isNotEmpty() && hasNext, total)
}

fun mergeCatalogPages(old: List<Release>, next: List<Release>) = (old + next).distinctBy { it.id }
