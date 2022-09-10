package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.*
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.nullGet
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

/**
 * Created by radiationx on 18.12.17.
 */
class ReleaseParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig
) {

    companion object {
        private const val VK_URL = "https://vk.com/anilibria?w=wall-37468416_493445"
    }

    fun parseRandomRelease(jsonItem: JSONObject): RandomRelease = RandomRelease(
        jsonItem.getString("code")
    )

    fun parseRelease(jsonItem: JSONObject): ReleaseItem {
        val item = ReleaseItem()
        item.id = jsonItem.getInt("id")
        item.code = jsonItem.getString("code")
        item.link = "${apiConfig.siteUrl}/release/${item.code}.html"
        item.names.addAll(jsonItem.getJSONArray("names").let { names ->
            (0 until names.length()).map {
                apiUtils.escapeHtml(names.getString(it)).toString()
            }
        })
        item.series = jsonItem.nullString("series")
        item.poster = "${apiConfig.baseImagesUrl}${jsonItem.nullString("poster")}"
        jsonItem.optJSONObject("favorite")?.also { jsonFavorite ->
            item.favoriteInfo.also {
                it.rating = jsonFavorite.getInt("rating")
                it.isAdded = jsonFavorite.getBoolean("added")
            }
        }
        item.torrentUpdate = jsonItem.nullString("last")?.let {
            try {
                it.toInt()
            } catch (ex: Exception) {
                item.torrentUpdate
            }
        } ?: item.torrentUpdate
        item.status = jsonItem.nullString("status")
        item.statusCode = jsonItem.nullString("statusCode")
        item.description = jsonItem.nullString("description")?.trim()

        item.announce = jsonItem.nullString("announce")?.trim()

        jsonItem.nullString("type")?.also {
            item.types.add(it)
        }

        jsonItem.optJSONArray("genres")?.also { jsonGenres ->
            for (j in 0 until jsonGenres.length()) {
                item.genres.add(jsonGenres.getString(j))
            }
        }

        jsonItem.optJSONArray("voices")?.also { jsonVoices ->
            for (j in 0 until jsonVoices.length()) {
                item.voices.add(jsonVoices.getString(j))
            }
        }

        jsonItem.nullString("year")?.also {
            item.seasons.add(it)
        }

        jsonItem.nullString("day")?.also {
            item.days.add(it)
        }
        return item
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
        val jsonItems = jsonResponse.getJSONArray("items")
        val resItems = releases(jsonItems)
        val pagination = Paginated(resItems)
        val jsonNav = jsonResponse.getJSONObject("pagination")
        jsonNav.nullGet("page")?.let { pagination.page = it.toString().toInt() }
        jsonNav.nullGet("perPage")?.let { pagination.perPage = it.toString().toInt() }
        jsonNav.nullGet("allPages")?.let { pagination.allPages = it.toString().toInt() }
        jsonNav.nullGet("allItems")?.let { pagination.allItems = it.toString().toInt() }
        return pagination
    }

    fun release(jsonResponse: JSONObject): ReleaseFull {
        val baseRelease = parseRelease(jsonResponse)
        val release = ReleaseFull(baseRelease)

        jsonResponse.optJSONObject("blockedInfo")?.also { jsonBlockedInfo ->
            release.blockedInfo.also {
                it.isBlocked = jsonBlockedInfo.getBoolean("blocked")
                it.reason = jsonBlockedInfo.nullString("reason")
            }
        }

        release.moonwalkLink = jsonResponse.nullString("moon")

        val onlineEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                ReleaseFull.Episode().also {
                    it.releaseId = release.id
                    it.id = jsonEpisode.optInt("id")
                    it.title = jsonEpisode.nullString("title")
                    it.urlSd = jsonEpisode.nullString("sd")
                    it.urlHd = jsonEpisode.nullString("hd")
                    it.urlFullHd = jsonEpisode.nullString("fullhd")
                }
            }
            .orEmpty()

        val sourceEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                SourceEpisode(
                    id = jsonEpisode.optInt("id"),
                    releaseId = release.id,
                    title = jsonEpisode.nullString("title"),
                    urlSd = jsonEpisode.nullString("srcSd").takeIf { it != VK_URL },
                    urlHd = jsonEpisode.nullString("srcHd").takeIf { it != VK_URL },
                    urlFullHd = jsonEpisode.nullString("srcFullHd").takeIf { it != VK_URL }
                )
            }
            .orEmpty()

        val rutubeEpisodes = jsonResponse
            .optJSONArray("playlist")
            ?.mapObjects { jsonEpisode ->
                val rutubeId = jsonEpisode
                    .nullString("rutube_id")
                    ?: return@mapObjects null
                RutubeEpisode(
                    id = jsonEpisode.optInt("id"),
                    releaseId = release.id,
                    title = jsonEpisode.nullString("title"),
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
                        releaseId = release.id,
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

        release.episodes.addAll(onlineEpisodes)
        release.sourceEpisodes.addAll(sourceEpisodes)
        release.externalPlaylists.addAll(externalPlaylists)
        release.rutubePlaylist.addAll(rutubeEpisodes)

        jsonResponse.getJSONArray("torrents")?.also { jsonTorrents ->
            for (j in 0 until jsonTorrents.length()) {
                jsonTorrents.optJSONObject(j)?.let { jsonTorrent ->
                    release.torrents.add(TorrentItem().apply {
                        id = jsonTorrent.optInt("id")
                        hash = jsonTorrent.nullString("hash")
                        leechers = jsonTorrent.optInt("leechers")
                        seeders = jsonTorrent.optInt("seeders")
                        completed = jsonTorrent.optInt("completed")
                        quality = jsonTorrent.nullString("quality")
                        series = jsonTorrent.nullString("series")
                        size = jsonTorrent.optLong("size")
                        url = "${apiConfig.baseImagesUrl}${jsonTorrent.nullString("url")}"
                    })
                }
            }
        }

        release.showDonateDialog = jsonResponse.optBoolean("showDonateDialog")

        return release
    }

}
