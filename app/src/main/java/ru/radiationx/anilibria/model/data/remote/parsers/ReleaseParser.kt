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

    private val fastSearchPatternSource = "<a[^>]*?href=\"[^\"]*?\\/release\\/[^\"]*?\"[^>]*?>[^<]*?<img[^>]*?>([\\s\\S]*?) \\/ ([\\s\\S]*?)(?:<\\/a>[^>]*?)?<\\/td>"
    private val idNamePatternSource = "\\/release\\/([\\s\\S]*?)\\.html"

    /*
    * 1.    String  Description
    * 2.    String  ELEMENT_CODE / idName
    * 3.    Int     Id
    * 4.    String  Image url
    * 5.    String  Title
    * */
    private val favoritesPatternSource = "<article[^>]*?class=\"favorites_block\"[^>]*?>[^<]*?<div[^>]*?>[^<]*?<p[^>]*?class=\"favorites_description\"[^>]*?>([\\s\\S]*?)<\\/p>[^<]*?<a[^>]*?href=\"\\/release\\/([\\s\\S]*?)\\.html\"[^>]*?>[^<]*?<\\/a>[^<]*?<a[^>]*?id=\"asd_fd_(\\d+)[^\"]*?\"[^>]*?>[^<]*?<\\/a>[^<]*?<\\/div>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[^<]*?<h2[^>]*?>([\\s\\S]*?)<\\/h2>"

    private val fastSearchPattern: Pattern by lazy {
        Pattern.compile(fastSearchPatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val idNamePattern: Pattern by lazy {
        Pattern.compile(idNamePatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val favoritesPattern: Pattern by lazy {
        Pattern.compile(favoritesPatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun fastSearch(httpResponse: String): List<SearchItem> {
        val result: MutableList<SearchItem> = mutableListOf()
        val matcher: Matcher = fastSearchPattern.matcher(httpResponse)
        while (matcher.find()) {
            result.add(SearchItem().apply {
                originalTitle = matcher.group(1)
                title = matcher.group(2)
            })
        }
        return result
    }

    fun genres(httpResponse: String): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        val jsonItems = JSONObject(httpResponse).getJSONArray("data")
        for (i in 0 until jsonItems.length()) {
            val genreText = jsonItems.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.substring(0, 1).toUpperCase() + genreText.substring(1)
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

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

    fun favorites(httpResponse: String): Paginated<List<ReleaseItem>> {
        val resItems = mutableListOf<ReleaseItem>()
        val matcher = favoritesPattern.matcher(httpResponse)
        while (matcher.find()) {
            val item = ReleaseItem()
            item.description = apiUtils.escapeHtml(matcher.group(1))
            item.code = matcher.group(2)
            item.id = matcher.group(3).toInt()
            item.poster = Api.BASE_URL_IMAGES + matcher.group(4)

            /*val titles = matcher.group(5).split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }*/
            resItems.add(item)
        }
        /*val jsonNav = responseJson.getJSONObject("navigation")
        pagination.total = jsonNav.get("total").toString().toInt()
        pagination.current = jsonNav.get("page").toString().toInt()
        pagination.allPages = jsonNav.get("total_pages").toString().toInt()*/
        return Paginated(resItems)
    }

    fun favorites2(httpResponse: String): FavoriteData {
        val resItems = mutableListOf<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = ReleaseItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")

            val matcher = idNamePattern.matcher(jsonItem.getString("link"))
            if (matcher.find()) {
                item.code = matcher.group(1)
            }

            item.description = jsonItem.getString("description")
            item.poster = Api.BASE_URL_IMAGES + jsonItem.get("image")

            val titles = jsonItem.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            /*if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }*/
            resItems.add(item)
        }
        val result = FavoriteData()
        result.sessId = responseJson.getString("sessId")
        result.items = Paginated(resItems)
        return result
    }

    fun comments(httpResponse: String): Paginated<List<Comment>> {
        val resItems = mutableListOf<Comment>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = Comment()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")
            item.forumId = jsonItem.getInt("forumId")
            item.topicId = jsonItem.getInt("topicId")
            item.date = jsonItem.getString("postDate")
            item.message = jsonItem.getString("postMessage")
            item.authorId = jsonItem.getInt("authorId")
            item.authorNick = jsonItem.getString("authorName")
            item.avatar = Api.BASE_URL_IMAGES + jsonItem.getString("avatar")
            item.userGroup = jsonItem.optInt("userGroup", 0)
            item.userGroupName = jsonItem.optString("userGroupName", null)
            resItems.add(item)
        }
        val pagination = Paginated(resItems)
        val jsonNav = responseJson.getJSONObject("navigation")
        jsonNav.nullGet("total")?.let { pagination.allItems = it.toString().toInt() }
        jsonNav.nullGet("page")?.let { pagination.page = it.toString().toInt() }
        jsonNav.nullGet("total_pages")?.let { pagination.allPages = it.toString().toInt() }
        return pagination
    }

    fun favXhr(httpResponse: String): Int {
        Log.e("S_DEF_LOG", "favXhr " + httpResponse)
        return JSONObject(httpResponse).getInt("COUNT")
    }
}
