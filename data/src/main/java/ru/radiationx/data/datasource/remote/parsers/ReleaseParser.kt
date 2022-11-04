package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.*
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.mapStrings
import ru.radiationx.shared.ktx.android.nullString
import ru.radiationx.shared.ktx.android.toStringsList
import java.util.*
import javax.inject.Inject

/**
 * Created by radiationx on 18.12.17.
 */
class ReleaseParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig,
    private val paginationParser: PaginationParser
) {

    companion object {
        private const val VK_URL = "https://vk.com/anilibria?w=wall-37468416_493445"
    }

    fun parseRandomRelease(jsonItem: JSONObject): RandomRelease = RandomRelease(
        jsonItem.getString("code")
    )

    fun parseRelease(jsonItem: JSONObject): ReleaseItem {
        val code = jsonItem.nullString("code")
        val names = jsonItem.getJSONArray("names").mapStrings {
            apiUtils.escapeHtml(it).toString()
        }
        val favoriteInfo = jsonItem.optJSONObject("favorite")?.let { jsonFavorite ->
            FavoriteInfo(
                rating = jsonFavorite.getInt("rating"),
                isAdded = jsonFavorite.getBoolean("added")
            )
        } ?: FavoriteInfo(0, false)
        return ReleaseItem(
            id = jsonItem.getInt("id"),
            code = code,
            names = names,
            series = jsonItem.nullString("series"),
            poster = "${apiConfig.baseImagesUrl}${jsonItem.nullString("poster")}",
            torrentUpdate = jsonItem.nullString("last")?.toIntOrNull() ?: 0,
            status = jsonItem.nullString("status"),
            statusCode = jsonItem.nullString("statusCode"),
            types = jsonItem.nullString("type")?.let { listOf(it) }.orEmpty(),
            genres = jsonItem.optJSONArray("genres")?.toStringsList().orEmpty(),
            voices = jsonItem.optJSONArray("voices")?.toStringsList().orEmpty(),
            seasons = jsonItem.nullString("year")?.let { listOf(it) }.orEmpty(),
            days = jsonItem.nullString("day")?.let { listOf(it) }.orEmpty(),
            description = jsonItem.nullString("description")?.trim(),
            announce = jsonItem.nullString("announce")?.trim(),
            favoriteInfo = favoriteInfo,
            isNew = false,
            link = "${apiConfig.siteUrl}/release/${code}.html"
        )
    }

    fun releases(jsonItems: JSONArray): List<ReleaseItem> {
        val resItems = mutableListOf<ReleaseItem>()
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            resItems.add(parseRelease(jsonItem))
        }
        return resItems
    }

    fun releases(jsonResponse: JSONObject): Paginated<List<ReleaseItem>> {
        return paginationParser.parse(jsonResponse) {
            releases(it)
        }
    }

    fun release(jsonResponse: JSONObject): ReleaseFull {
        val baseRelease = parseRelease(jsonResponse)
        val blockedInfo = jsonResponse.optJSONObject("blockedInfo")?.let { it ->
            BlockedInfo(
                isBlocked = it.getBoolean("blocked"),
                reason = it.nullString("reason")
            )
        } ?: BlockedInfo(false, null)


        val onlineEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                parseSourceTypes(jsonEpisode)
                    ?.takeIf { it.isAnilibria }
                    ?: return@mapObjects null
                ReleaseFull.Episode().also {
                    it.releaseId = baseRelease.id
                    it.id = jsonEpisode.optInt("id")
                    it.title = jsonEpisode.nullString("title")
                    it.urlSd = jsonEpisode.nullString("sd")
                    it.urlHd = jsonEpisode.nullString("hd")
                    it.urlFullHd = jsonEpisode.nullString("fullhd")
                    it.updatedAt = Date(jsonEpisode.optInt("updated_at") * 1000L)
                    it.skips = parsePlayerSkips(jsonEpisode)
                }
            }
            ?.filterNotNull()
            .orEmpty()

        val sourceEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                parseSourceTypes(jsonEpisode)
                    ?.takeIf { it.isAnilibria }
                    ?: return@mapObjects null
                SourceEpisode(
                    id = jsonEpisode.optInt("id"),
                    releaseId = baseRelease.id,
                    updatedAt = Date(jsonEpisode.optInt("updated_at") * 1000L),
                    title = jsonEpisode.nullString("title"),
                    urlSd = jsonEpisode.nullString("srcSd").takeIf { it != VK_URL },
                    urlHd = jsonEpisode.nullString("srcHd").takeIf { it != VK_URL },
                    urlFullHd = jsonEpisode.nullString("srcFullHd").takeIf { it != VK_URL }
                )
            }
            ?.filterNotNull()
            .orEmpty()

        val rutubeEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                parseSourceTypes(jsonEpisode)
                    ?.takeIf { it.isRutube }
                    ?: return@mapObjects null
                val rutubeId = jsonEpisode
                    .nullString("rutube_id")
                    ?: return@mapObjects null
                RutubeEpisode(
                    id = jsonEpisode.optInt("id"),
                    releaseId = baseRelease.id,
                    title = jsonEpisode.nullString("title"),
                    updatedAt = Date(jsonEpisode.optInt("updated_at") * 1000L),
                    rutubeId = rutubeId,
                    url = "https://rutube.ru/play/embed/$rutubeId"
                )
            }
            ?.filterNotNull()
            .orEmpty()

        val externalPlaylists = jsonResponse
            .optJSONArray("externalPlaylist")
            ?.mapObjects { jsonPlaylist ->
                val episodes = jsonPlaylist.getJSONArray("episodes").mapObjects { jsonEpisode ->
                    ExternalEpisode(
                        id = jsonEpisode.getInt("id"),
                        releaseId = baseRelease.id,
                        title = jsonEpisode.nullString("title"),
                        url = jsonEpisode.nullString("url")
                    )
                }

                ExternalPlaylist(
                    tag = jsonPlaylist.getString("tag"),
                    title = jsonPlaylist.getString("title"),
                    actionText = jsonPlaylist.getString("actionText"),
                    episodes = episodes
                )
            }
            .orEmpty()


        val torrents = jsonResponse.getJSONArray("torrents")?.mapObjects { jsonTorrent ->
            TorrentItem().apply {
                id = jsonTorrent.optInt("id")
                hash = jsonTorrent.nullString("hash")
                leechers = jsonTorrent.optInt("leechers")
                seeders = jsonTorrent.optInt("seeders")
                completed = jsonTorrent.optInt("completed")
                quality = jsonTorrent.nullString("quality")
                series = jsonTorrent.nullString("series")
                size = jsonTorrent.optLong("size")
                url = "${apiConfig.baseImagesUrl}${jsonTorrent.nullString("url")}"
                date = Date(jsonTorrent.optInt("ctime") * 1000L)
            }
        }.orEmpty()

        return ReleaseFull(
            item = baseRelease,
            showDonateDialog = jsonResponse.optBoolean("showDonateDialog"),
            blockedInfo = blockedInfo,
            moonwalkLink = jsonResponse.nullString("moon"),
            episodes = onlineEpisodes,
            sourceEpisodes = sourceEpisodes,
            externalPlaylists = externalPlaylists,
            rutubePlaylist = rutubeEpisodes,
            torrents = torrents
        )
    }

    private fun parseSourceTypes(jsonResponse: JSONObject): SourceTypes? {
        return jsonResponse.optJSONObject("sources")?.let {
            SourceTypes(
                it.optBoolean("is_rutube", false),
                it.optBoolean("is_anilibria", false)
            )
        }
    }

    private fun parsePlayerSkips(jsonResponse: JSONObject): PlayerSkips? {
        return jsonResponse.optJSONObject("skips")?.let {
            val opening = it.optJSONArray("opening")?.let { parseSkipRange(it) }
            val ending = it.optJSONArray("ending")?.let { parseSkipRange(it) }
            PlayerSkips(opening, ending)
        }
    }

    private fun parseSkipRange(jsonArray: JSONArray): PlayerSkips.Skip? {
        val first = jsonArray.optInt(0, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE } ?: return null
        val last = jsonArray.optInt(1, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE } ?: return null
        return PlayerSkips.Skip(first * 1000L, last * 1000L)
    }

}
