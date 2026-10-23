package tv.aniliberty.next

import java.net.URLEncoder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.json.JSONArray
import org.json.JSONObject

fun number(value: Double): String =
    java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

fun imageUrl(value: String?): String? =
    value
        ?.trim()
        ?.takeIf { it.isNotBlank() && it != "null" }
        ?.let {
            when {
                it.startsWith("https://") -> it
                it.startsWith("//") -> "https:$it"
                it.startsWith("/") -> "https://cdn.anilibria.top$it"
                else -> null
            }
        }

private fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key).trim()

private fun JSONObject.image(): String? =
    imageUrl(optJSONObject("optimized")?.text("preview")) ?: imageUrl(text("preview"))

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }

private fun JSONObject.display(key: String): String =
    optJSONObject(key)?.let { it.text("description").ifBlank { it.text("value") } }.orEmpty()

fun parseRelease(json: JSONObject): Release {
    val episodes =
        json
            .optJSONArray("episodes")
            .objects()
            .mapNotNull { e ->
                val id = e.text("id")
                val ordinal = e.optDouble("ordinal", Double.NaN)
                if (id.isBlank() || !ordinal.isFinite()) null
                else
                    Episode(
                        id,
                        ordinal,
                        e.text("name"),
                        e.optJSONObject("preview")?.image(),
                        e.optLong("duration", 0).coerceAtLeast(0),
                        listOf("1080p" to "hls_1080", "720p" to "hls_720", "480p" to "hls_480")
                            .mapNotNull { (quality, key) ->
                                e.text(key)
                                    .takeIf { it.startsWith("https://") }
                                    ?.let { quality to it }
                            },
                        e.optDouble("sort_order", ordinal),
                        e.text("updated_at"),
                        e.optJSONObject("opening")?.let {
                            EpisodeSkip(
                                it.optDouble("start", Double.NaN),
                                it.optDouble("stop", Double.NaN),
                            )
                        },
                        e.optJSONObject("ending")?.let {
                            EpisodeSkip(
                                it.optDouble("start", Double.NaN),
                                it.optDouble("stop", Double.NaN),
                            )
                        },
                    )
            }
            .sortedWith(compareBy<Episode> { it.order }.thenBy { it.ordinal })
    return Release(
            json.getInt("id"),
            json.text("alias"),
            json.optJSONObject("name")?.text("main").orEmpty(),
            json.optJSONObject("name")?.text("english").orEmpty(),
            json.optJSONObject("poster")?.image(),
            json.text("year"),
            json.display("season"),
            json.display("type"),
            json.display("age_rating"),
            json.optJSONArray("genres").objects().map { it.text("name") },
            json.text("description"),
            episodes,
            json.optJSONArray("members").objects().map {
                Credit(it.display("role"), it.text("nickname"))
            },
            json.optBoolean("is_blocked_by_geo") || json.optBoolean("is_blocked_by_copyrights"),
            json.optJSONObject("latest_episode")?.let {
                parseRelease(
                        JSONObject()
                            .put("id", json.getInt("id"))
                            .put("episodes", JSONArray().put(it))
                    )
                    .episodes
                    .firstOrNull()
            },
            ExternalIds(
                json.optJSONObject("shikimori")?.optInt("id")?.takeIf { it > 0 },
                json.optJSONObject("mal")?.optInt("id")?.takeIf { it > 0 },
            ),
            listOf("Shikimori" to "shikimori", "MyAnimeList" to "mal").mapNotNull { (source, key) ->
                json.optJSONObject(key)?.let { site ->
                    site
                        .optDouble("rating", Double.NaN)
                        .takeIf { it.isFinite() && it in 1.0..10.0 }
                        ?.let { Rating(source, it, site.optInt("votes").takeIf { it > 0 }) }
                }
            },
        )
        .copy(
            ongoing =
                if (json.has("is_ongoing") && !json.isNull("is_ongoing"))
                    json.getBoolean("is_ongoing")
                else null,
            updatedAt = json.text("updated_at"),
            totalEpisodes = json.optInt("episodes_total", -1).takeIf { it > 0 },
            releasedEpisodes = json.optJSONArray("episodes")?.let { publishedEpisodeCount(it) },
            favoriteCount =
                if (
                    json.has("added_in_users_favorites") && !json.isNull("added_in_users_favorites")
                )
                    json.optLong("added_in_users_favorites", -1).takeIf { it >= 0 }
                else null,
        )
}

internal fun publishedEpisodeCount(episodes: JSONArray): Int =
    episodes.objects().map { it.text("id") }.filter { it.isNotBlank() }.distinct().size

internal fun parseReleasedEpisodeCounts(body: String): Map<Int, Int> =
    JSONObject(body)
        .optJSONArray("data")
        .objects()
        .mapNotNull { release ->
            val id = release.optInt("id", -1)
            val episodes = release.optJSONArray("episodes")
            if (id > 0 && episodes != null) id to publishedEpisodeCount(episodes) else null
        }
        .toMap()

private suspend fun <T> optional(block: suspend () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

class Api(private val transport: AccountTransport = AccountHttpTransport()) {
    private val cache = linkedMapOf<Int, Release>()
    private val discoveryCache = linkedMapOf<Int, Pair<Long, Discovery>>()

    private suspend fun request(path: String): String = transport.request(path, "GET", null, null)

    suspend fun home(): HomeData = supervisorScope {
        val latest = async { latest() }
        val schedule = async {
            optional {
                val json = JSONObject(request("/anime/schedule/now"))
                listOf("yesterday", "today", "tomorrow").map { day ->
                    parseScheduleEntries(json.optJSONArray(day) ?: JSONArray())
                }
            }
        }
        val franchises = async { optional { randomFeatures(HomeSection.FRANCHISES) } }
        val genres = async { optional { randomFeatures(HomeSection.GENRES) } }
        val videos = async { optional { videos(9) } }
        HomeData(
            latest.await(),
            schedule.await(),
            franchises.await(),
            genres.await(),
            videos.await(),
        )
    }

    suspend fun randomFeatures(section: HomeSection): List<Feature> =
        when (section) {
            HomeSection.GENRES -> features("/anime/genres/random?limit=4", false)
            HomeSection.FRANCHISES -> features("/anime/franchises/random?limit=3", true)
            else -> error("Этот раздел не поддерживает случайную подборку")
        }

    private suspend fun features(path: String, franchise: Boolean): List<Feature> =
        JSONArray(request(path))
            .objects()
            .map { feature ->
                val subtitle =
                    if (franchise)
                        listOf(
                                listOf(feature.text("first_year"), feature.text("last_year"))
                                    .filter(String::isNotBlank)
                                    .distinct()
                                    .joinToString("–"),
                                feature
                                    .text("total_releases")
                                    .takeIf { it.isNotBlank() }
                                    ?.let { "$it частей" }
                                    .orEmpty(),
                            )
                            .filter(String::isNotBlank)
                            .joinToString(" · ")
                    else
                        feature
                            .text("total_releases")
                            .takeIf { it.isNotBlank() }
                            ?.let { "$it релизов" }
                            .orEmpty()
                Feature(
                    feature.text("id"),
                    feature.text("name"),
                    subtitle,
                    feature.optJSONObject("image")?.image(),
                )
            }
            .distinctBy { it.id }

    private suspend fun videos(limit: Int): List<HomeVideo> =
        parseHomeVideos(request("/media/videos?limit=$limit"))

    suspend fun releaseTorrents(releaseId: Int): List<TorrentLink> =
        JSONArray(request("/anime/torrents/release/$releaseId")).objects().map { t ->
            TorrentLink(
                t.text("label").ifBlank {
                    listOf(t.display("quality"), t.display("codec"))
                        .filter(String::isNotBlank)
                        .joinToString(" · ")
                },
                t.text("description"),
                t.text("magnet").takeIf { it.startsWith("magnet:") },
            )
        }

    private suspend fun recentEpisodesPage(page: Int): SectionData {
        val body = request(catalogPath(Screen.Catalog(), page))
        val parsed = parseCatalogPage(body, page)
        if (parsed.releases.isEmpty()) return SectionData()
        val ids = parsed.releases.joinToString(",") { it.id.toString() }
        val details =
            JSONObject(
                    request(
                        "/anime/releases/list?ids=$ids&limit=30&include=id,episodes.id,episodes.ordinal,episodes.name,episodes.updated_at,episodes.preview,episodes.sort_order,episodes.duration"
                    )
                )
                .optJSONArray("data")
                .objects()
                .associate { it.getInt("id") to parseRelease(it).episodes }
        val items =
            parsed.releases
                .map { release ->
                    val episodes = details[release.id] ?: error("Не удалось получить серии релиза")
                    release.copy(latestEpisode = episodes.maxByOrNull { it.updatedAt })
                }
                .filter { it.latestEpisode != null }
        val cutoff = java.time.LocalDate.now().minusMonths(1)
        // The catalogue is ordered by fresh_at; only its ordered dates can end pagination.
        val freshDates =
            JSONObject(body).optJSONArray("data").objects().map { json ->
                runCatching {
                        java.time.OffsetDateTime.parse(json.text("fresh_at"))
                            .atZoneSameInstant(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .getOrNull()
            }
        val reachedEnd =
            freshDates.isNotEmpty() && freshDates.all { it != null && it.isBefore(cutoff) }
        return SectionData(
            releases = recentEpisodeReleases(items),
            more = parsed.more && !reachedEnd,
        )
    }

    suspend fun section(section: HomeSection, page: Int = 1): SectionData =
        when (section) {
            HomeSection.LATEST -> recentEpisodesPage(page)
            HomeSection.SCHEDULE ->
                SectionData(releases = parseScheduleWeek(request("/anime/schedule/week")))
            HomeSection.FRANCHISES ->
                SectionData(
                    features = features("/anime/franchises", true).sortedBy { it.title.lowercase() }
                )
            HomeSection.GENRES ->
                SectionData(
                    features = features("/anime/genres", false).sortedBy { it.title.lowercase() }
                )
            HomeSection.VIDEOS ->
                videos(page * 30).let { SectionData(videos = it, more = it.size >= page * 30) }
        }

    suspend fun discovery(releaseId: Int, force: Boolean = false): Discovery {
        synchronized(discoveryCache) { if (force) null else discoveryCache[releaseId] }
            ?.takeIf { android.os.SystemClock.elapsedRealtime() - it.first < 86400000 }
            ?.let {
                return it.second
            }
        val result = supervisorScope {
            val groups = async {
                optional {
                    parseFranchiseGroups(JSONArray(request("/anime/franchises/release/$releaseId")))
                }
            }
            val recommendations = async {
                optional {
                    uniqueRecommendations(
                        releaseId,
                        JSONArray(
                                request(
                                    "/anime/releases/recommended?release_id=$releaseId&limit=12"
                                )
                            )
                            .objects()
                            .map(::parseRelease),
                    )
                }
            }
            val related = groups.await()
            Discovery(
                related,
                recommendations.await()?.let {
                    uniqueRecommendations(releaseId, it, related.orEmpty())
                },
            )
        }
        if (result.franchises != null && result.recommendations != null)
            synchronized(discoveryCache) {
                if (discoveryCache.size >= 32) discoveryCache.remove(discoveryCache.keys.first())
                discoveryCache[releaseId] = android.os.SystemClock.elapsedRealtime() to result
            }
        return result
    }

    suspend fun franchise(id: String): List<Release> =
        JSONObject(request("/anime/franchises/${URLEncoder.encode(id,"UTF-8")}"))
            .optJSONArray("franchise_releases")
            .objects()
            .sortedBy { it.optInt("sort_order") }
            .mapNotNull { it.optJSONObject("release")?.let(::parseRelease) }

    suspend fun libraryReleases(ids: List<Int>): List<Release> {
        val order = ids.distinct().withIndex().associate { it.value to it.index }
        return order.keys
            .chunked(30)
            .flatMap { batch ->
                JSONObject(
                        request(
                            "/anime/releases/list?ids=${batch.joinToString(",")}&limit=30&exclude=episodes,torrents,members,sponsors"
                        )
                    )
                    .optJSONArray("data")
                    .objects()
                    .map(::parseRelease)
            }
            .sortedBy { order[it.id] ?: Int.MAX_VALUE }
    }

    suspend fun latest(limit: Int = 9): List<Release> =
        JSONArray(request("/anime/releases/latest?limit=$limit")).objects().map(::parseRelease)

    suspend fun catalog(screen: Screen.Catalog, page: Int = screen.page): CatalogPage {
        val result = parseCatalogPage(request(catalogPath(screen, page)), page)
        val ids = result.releases.map { it.id }
        if (ids.isEmpty()) return result
        // One batch per catalogue page; planned episodes_total is never a released count.
        val counts =
            try {
                parseReleasedEpisodeCounts(
                    request(
                        "/anime/releases/list?ids=${ids.joinToString(",")}&limit=${ids.size}&include=id,episodes.id"
                    )
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emptyMap<Int, Int>()
            }
        return result.copy(
            releases =
                result.releases.map { release ->
                    release.copy(releasedEpisodes = counts[release.id])
                }
        )
    }

    private var references: Map<String, List<CatalogChoice>>? = null

    suspend fun catalogReferences(): Map<String, List<CatalogChoice>> {
        references?.let {
            return it
        }
        val result = supervisorScope {
            catalogReferencePaths.keys
                .associateWith { key ->
                    async {
                        parseCatalogChoices(
                            request("/anime/catalog/references/$key"),
                            key == "years",
                        )
                    }
                }
                .mapValues { it.value.await() }
        }
        references = result
        return result
    }

    suspend fun randomRelease(): Release =
        JSONArray(request("/anime/releases/random?limit=1")).getJSONObject(0).let(::parseRelease)

    suspend fun release(id: Int, refresh: Boolean = false): Release {
        if (!refresh)
            synchronized(cache) { cache[id] }
                ?.let {
                    return it
                }
        val item = parseRelease(JSONObject(request("/anime/releases/$id?exclude=torrents")))
        synchronized(cache) {
            if (cache.size >= 24) cache.remove(cache.keys.first())
            cache[id] = item
        }
        return item
    }
}

data class Feature(val id: String, val title: String, val subtitle: String, val image: String?)

data class HomeData(
    val latest: List<Release>,
    val schedule: List<List<Release>>?,
    val franchises: List<Feature>?,
    val genres: List<Feature>?,
    val videos: List<HomeVideo>? = null,
)

data class HomeVideo(
    val id: String,
    val title: String,
    val subtitle: String,
    val image: String?,
    val url: String,
)

data class ExternalIds(val shikimori: Int? = null, val mal: Int? = null)

data class Rating(val source: String, val value: Double, val votes: Int?)

data class FranchiseGroup(val id: String, val title: String, val releases: List<Release>)

data class Discovery(val franchises: List<FranchiseGroup>?, val recommendations: List<Release>?)

fun uniqueRecommendations(
    currentId: Int,
    releases: List<Release>,
    franchises: List<FranchiseGroup> = emptyList(),
): List<Release> {
    val related = franchises.flatMap { it.releases }.map { it.id }.toSet() + currentId
    return releases.filter { it.id > 0 && it.id !in related }.distinctBy { it.id }
}

internal fun mergeDiscovery(previous: Discovery?, next: Discovery) =
    Discovery(
            next.franchises ?: previous?.franchises,
            next.recommendations ?: previous?.recommendations,
        )
        .let { value ->
            value.copy(
                recommendations =
                    value.recommendations?.let {
                        uniqueRecommendations(-1, it, value.franchises.orEmpty())
                    }
            )
        }

fun parseFranchiseGroups(json: JSONArray): List<FranchiseGroup> =
    json
        .objects()
        .map { group ->
            FranchiseGroup(
                group.text("id"),
                group.text("name"),
                group
                    .optJSONArray("franchise_releases")
                    .objects()
                    .sortedBy { it.optDouble("sort_order", Double.MAX_VALUE) }
                    .mapNotNull { it.optJSONObject("release")?.let(::parseRelease) }
                    .distinctBy { it.id },
            )
        }
        .filter { it.id.isNotBlank() && it.releases.isNotEmpty() }
        .distinctBy { it.id }

enum class HomeSection(val title: String) {
    LATEST("Новые эпизоды"),
    SCHEDULE("Расписание релизов"),
    FRANCHISES("Франшизы"),
    GENRES("Жанры"),
    VIDEOS("Видео команды"),
}

data class SectionData(
    val releases: List<Release> = emptyList(),
    val features: List<Feature> = emptyList(),
    val videos: List<HomeVideo> = emptyList(),
    val more: Boolean = false,
)

data class TorrentLink(val title: String, val description: String, val magnet: String?)

fun replaceHomeFeatures(home: HomeData, section: HomeSection, features: List<Feature>): HomeData =
    when (section) {
        HomeSection.GENRES -> home.copy(genres = features.take(4))
        HomeSection.FRANCHISES -> home.copy(franchises = features.take(3))
        else -> error("Раздел не является случайной подборкой")
    }

/** /media/videos returns an array, as consumed by the website's videoContentProxy. */
fun parseHomeVideos(body: String): List<HomeVideo> {
    val data =
        if (body.trimStart().startsWith("[")) JSONArray(body)
        else JSONObject(body).optJSONArray("data") ?: JSONArray()
    return data
        .objects()
        .map { item ->
            val origin = item.optJSONObject("origin")
            val direct = item.text("url").takeIf { it.startsWith("https://") }
            val youtubeId = item.text("video_id").takeIf { Regex("[A-Za-z0-9_-]{11}").matches(it) }
            val url =
                direct
                    ?: if (
                        origin?.optJSONObject("type")?.text("value")?.startsWith("YOUTUBE") ==
                            true && youtubeId != null
                    )
                        "https://www.youtube.com/watch?v=$youtubeId"
                    else ""
            HomeVideo(
                item.text("id"),
                item.text("title"),
                listOf(
                        origin?.text("title").orEmpty(),
                        updateTimestamp(item.text("created_at")).substringBefore(" "),
                    )
                    .filter(String::isNotBlank)
                    .joinToString(" · "),
                item.optJSONObject("image")?.image(),
                url,
            )
        }
        .distinctBy { it.id }
}

internal fun mergeSectionData(previous: SectionData, next: SectionData): SectionData {
    val releases = (previous.releases + next.releases).distinctBy { it.id }
    val features = (previous.features + next.features).distinctBy { it.id }
    val videos = (previous.videos + next.videos).distinctBy { it.id }
    val grew =
        releases.size > previous.releases.size ||
            features.size > previous.features.size ||
            videos.size > previous.videos.size
    return SectionData(releases, features, videos, next.more && grew)
}

internal fun filterSectionData(data: SectionData, query: String): SectionData {
    val tokens =
        query.trim().lowercase().replace('ё', 'е').split(Regex("\\s+")).filter { it.isNotBlank() }
    fun matches(vararg fields: String): Boolean {
        val text = fields.joinToString(" ").lowercase().replace('ё', 'е')
        return tokens.all { text.contains(it) }
    }
    return data.copy(
        releases = data.releases.filter { matches(it.title, it.english) },
        features = data.features.filter { matches(it.title) },
        videos = data.videos.filter { matches(it.title) },
    )
}

internal val scheduleDays =
    listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

internal fun parseScheduleEntries(entries: JSONArray): List<Release> =
    entries
        .objects()
        .mapNotNull { entry ->
            val json = entry.optJSONObject("release") ?: return@mapNotNull null
            val published = entry.optJSONObject("published_release_episode")
            if (published != null) json.put("latest_episode", published)
            val day = json.optJSONObject("publish_day")
            val described =
                scheduleDays
                    .indexOfFirst { it.equals(day?.optString("description"), true) }
                    .takeIf { it >= 0 }
                    ?.plus(1)
            val number =
                published?.optDouble("ordinal", Double.NaN)?.takeIf { it.isFinite() && it > 0 }
                    ?: entry.optDouble("next_release_episode_number", Double.NaN).takeIf {
                        it.isFinite() && it > 0
                    }
            parseRelease(json)
                .copy(
                    publishDay = described ?: day?.optInt("value")?.takeIf { it in 1..7 },
                    scheduledEpisode = number,
                    fullSeason = entry.optBoolean("full_season_is_released"),
                )
        }
        .distinctBy { it.id }

internal fun parseScheduleWeek(body: String): List<Release> =
    parseScheduleEntries(
        if (body.trimStart().startsWith("[")) JSONArray(body)
        else JSONObject(body).optJSONArray("data") ?: JSONArray()
    )

internal fun scheduleEpisodeLabel(release: Release?): String =
    when {
        release?.fullSeason == true -> "Полный сезон"
        release?.scheduledEpisode != null -> "${number(release.scheduledEpisode)} эп."
        release?.latestEpisode != null -> "${number(release.latestEpisode.ordinal)} эп."
        else -> "Эпизод —"
    }

internal fun filterScheduleSources(
    items: List<Release>,
    favorites: Set<Int>,
    collections: Set<Int>,
    favoritesOnly: Boolean,
    collectionsOnly: Boolean,
): List<Release> =
    if (!favoritesOnly && !collectionsOnly) items
    else
        items.filter {
            (favoritesOnly && it.id in favorites) || (collectionsOnly && it.id in collections)
        }
