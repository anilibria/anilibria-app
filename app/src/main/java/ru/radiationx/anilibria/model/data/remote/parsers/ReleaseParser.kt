package ru.radiationx.anilibria.model.data.remote.parsers

import android.util.Log
import ru.radiationx.anilibria.extension.nullGet
import ru.radiationx.anilibria.extension.nullString
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.12.17.
 */
class ReleaseParser(private val apiUtils: IApiUtils) {

    private fun parseRelease(jsonItem: JSONObject): ReleaseItem {
        val item = ReleaseItem()
        item.id = jsonItem.getInt("id")
        item.code = jsonItem.getString("code")
        item.names.addAll(jsonItem.getJSONArray("names").let { names ->
            (0 until names.length()).map {
                apiUtils.escapeHtml(names.getString(it)).toString()
            }
        })
        item.series = jsonItem.nullString("series")
        item.poster = Api.BASE_URL_IMAGES + jsonItem.nullString("poster")
        item.posterFull = Api.BASE_URL_IMAGES + jsonItem.nullString("posterFull")
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
        item.description = jsonItem.nullString("description")?.trim()

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

    fun releases(httpResponse: String): Paginated<List<ReleaseItem>> {
        val resItems = mutableListOf<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonData = responseJson.getJSONObject("data")
        val jsonItems = jsonData.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            resItems.add(parseRelease(jsonItem))
        }
        val pagination = Paginated(resItems)
        val jsonNav = jsonData.getJSONObject("pagination")
        jsonNav.nullGet("page")?.let { pagination.page = it.toString().toInt() }
        jsonNav.nullGet("perPage")?.let { pagination.perPage = it.toString().toInt() }
        jsonNav.nullGet("allPages")?.let { pagination.allPages = it.toString().toInt() }
        jsonNav.nullGet("allItems")?.let { pagination.allItems = it.toString().toInt() }
        return pagination
    }

    fun release(httpResponse: String): ReleaseFull {
        val responseJson = JSONObject(httpResponse).getJSONObject("data")
        val baseRelease = parseRelease(responseJson)
        val release = ReleaseFull(baseRelease)

        responseJson.optJSONObject("blockedInfo")?.also {jsonBlockedInfo->
            release.blockedInfo.also {
                it.isBlocked = jsonBlockedInfo.getBoolean("blocked")
                it.reason = jsonBlockedInfo.nullString("reason")
            }
        }

        responseJson.nullString("moon")?.also {
            val matcher = Pattern.compile("<iframe[^>]*?src=\"([^\"]*?)\"[^>]*?>").matcher(it)
            if (matcher.find()) {
                var mwUrl = matcher.group(1)
                if (mwUrl.substring(0, 2) == "//") {
                    mwUrl = "https:$mwUrl"
                }
                release.moonwalkLink = mwUrl
            }
        }

        responseJson.optJSONArray("playlist")?.also { jsonPlaylist ->
            for (j in 0 until jsonPlaylist.length()) {
                val jsonEpisode = jsonPlaylist.getJSONObject(j)

                val episodeId: Int = jsonEpisode.optInt("id")
                val episodeTitle = jsonEpisode.nullString("title")

                ReleaseFull.Episode().also {
                    it.releaseId = release.id
                    it.id = episodeId
                    it.title = episodeTitle
                    it.urlSd = jsonEpisode.nullString("sd")
                    it.urlHd = jsonEpisode.nullString("hd")
                    it.type = ReleaseFull.Episode.Type.ONLINE
                    release.episodes.add(it)
                }

                ReleaseFull.Episode().also {
                    it.releaseId = release.id
                    it.id = episodeId
                    it.title = episodeTitle
                    it.urlSd = jsonEpisode.nullString("srcSd")
                    it.urlHd = jsonEpisode.nullString("srcHd")
                    it.type = ReleaseFull.Episode.Type.SOURCE
                    release.episodesSource.add(it)
                }
            }
        }

        responseJson.getJSONArray("torrents")?.also { jsonTorrents ->
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
                        url = "${Api.SITE_URL}${jsonTorrent.nullString("url")}"
                    })
                }
            }
        }

        release.showDonateDialog = responseJson.optBoolean("showDonateDialog")

        return release
    }


}
