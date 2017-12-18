package ru.radiationx.anilibria.data.api.mappers

import android.text.Html
import org.json.JSONObject
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.GenreItem
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.12.17.
 */
object ReleasesMapper {
    fun genres(httpResponse: String): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        val jsonItems = JSONObject(httpResponse).getJSONArray("data")
        result.add(GenreItem().apply {
            title = "Все"
            value = ""
        })
        for (i in 0 until jsonItems.length()) {
            val genreText = jsonItems.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.substring(0, 1).toUpperCase() + genreText.substring(1);
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

    fun releases(httpResponse: String): Paginated<ArrayList<ReleaseItem>> {
        val resItems = ArrayList<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = ReleaseItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")

            val titles = jsonItem.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (titles.isNotEmpty()) {
                item.originalTitle = Html.fromHtml(titles[0]).toString()
                if (titles.size > 1) {
                    item.title = Html.fromHtml(titles[1]).toString()
                }
            }

            item.torrentLink = Api.BASE_URL + jsonItem.getString("torrent_link")
            item.link = Api.BASE_URL + jsonItem.getString("link")
            item.image = Api.BASE_URL + jsonItem.getString("image")
            item.episodesCount = jsonItem.getString("episode")
            item.description = Html.fromHtml(jsonItem.getString("description")).toString().trim()

            val jsonSeasons = jsonItem.getJSONArray("season")
            for (j in 0 until jsonSeasons.length()) {
                item.seasons.add(jsonSeasons.getString(j))
            }

            val jsonVoices = jsonItem.getJSONArray("voices")
            for (j in 0 until jsonVoices.length()) {
                item.voices.add(jsonVoices.getString(j))
            }

            val jsonGenres = jsonItem.getJSONArray("genres")
            for (j in 0 until jsonGenres.length()) {
                item.genres.add(jsonGenres.getString(j))
            }

            val jsonTypes = jsonItem.getJSONArray("types")
            for (j in 0 until jsonTypes.length()) {
                item.types.add(jsonTypes.getString(j))
            }

            resItems.add(item)
        }
        val pagination = Paginated(resItems)
        val jsonNav = responseJson.getJSONObject("navigation")
        pagination.total = jsonNav.get("total").toString().toInt()
        pagination.current = jsonNav.get("page").toString().toInt()
        pagination.allPages = jsonNav.get("total_pages").toString().toInt()
        return pagination
    }

    fun release(httpResponse: String): ReleaseItem {
        val release = ReleaseItem()

        val responseJson = JSONObject(httpResponse)
        //item.setId(responseJson.getInt("id"));

        val titles = responseJson.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (titles.size > 0) {
            release.originalTitle = Html.fromHtml(titles[0]).toString()
            if (titles.size > 1) {
                release.title = Html.fromHtml(titles[1]).toString()
            }
        }

        release.torrentLink = Api.BASE_URL + responseJson.getString("torrent_link")
        //item.setLink(responseJson.getString("link"));
        release.image = Api.BASE_URL + responseJson.getString("image")
        //release.setEpisodesCount(responseJson.getString("episode"));
        release.description = Html.fromHtml(responseJson.getString("description")).toString().trim()

        val jsonSeasons = responseJson.getJSONArray("season")
        for (j in 0 until jsonSeasons.length()) {
            release.seasons.add(jsonSeasons.getString(j))
        }

        val jsonVoices = responseJson.getJSONArray("voices")
        for (j in 0 until jsonVoices.length()) {
            release.voices.add(jsonVoices.getString(j))
        }

        val jsonGenres = responseJson.getJSONArray("genres")
        for (j in 0 until jsonGenres.length()) {
            release.genres.add(jsonGenres.getString(j))
        }

        val jsonTypes = responseJson.getJSONArray("types")
        for (j in 0 until jsonTypes.length()) {
            release.types.add(jsonTypes.getString(j))
        }

        val jsonEpisodes = responseJson.getJSONArray("Uppod")
        for (j in 0 until jsonEpisodes.length()) {
            val jsonEpisode = jsonEpisodes.getJSONObject(j)
            val episode = ReleaseItem.Episode()
            episode.title = jsonEpisode.getString("comment")
            episode.urlSd = jsonEpisode.getString("file")
            episode.urlHd = jsonEpisode.getString("filehd")
            release.episodes.add(episode)
        }

        val jsonMoonwalk = responseJson.getString("Moonwalk")
        jsonMoonwalk?.let {
            val matcher = Pattern.compile("<iframe[^>]*?src=\"([^\"]*?)\"[^>]*?>").matcher(it)
            if (matcher.find()) {
                release.moonwalkLink = "https:${matcher.group(1)}"
            }
        }

        release.isFull = true

        return release
    }
}