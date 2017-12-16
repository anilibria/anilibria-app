package ru.radiationx.anilibria.data.api.releases;

import android.text.Html
import io.reactivex.Observable
import org.json.JSONObject
import ru.radiationx.anilibria.data.Client
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.GenreItem
import ru.radiationx.anilibria.data.api.Paginated

/* Created by radiationx on 31.10.17. */

class Releases {
    fun getGenres(): Observable<List<GenreItem>> {
        return Observable.fromCallable {
            val url = "https://www.anilibria.tv/api/api.php?action=tags"
            val response = Client.instance.get(url)
            val result: MutableList<GenreItem> = mutableListOf()
            val jsonItems = JSONObject(response).getJSONArray("data")
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
            result
        }
    }

    fun search(name: String, genre: String, page: Int):
            Observable<Paginated<ArrayList<ReleaseItem>>> {
        return Observable.fromCallable {
            val url = "https://www.anilibria.tv/api/api.php?action=search&genre=$genre&name=$name&PAGEN_1=$page"
            val response = Client.instance.get(url)
            parseItems(response)
        }
    }

    fun getItems(page: Int): Observable<Paginated<ArrayList<ReleaseItem>>> {
        return Observable.fromCallable {
            val url = "https://www.anilibria.tv/api/api.php?PAGEN_1=$page"
            val response = Client.instance.get(url)
            parseItems(response)
        }
    }

    @Throws(Exception::class)
    private fun parseItems(responseText: String?): Paginated<ArrayList<ReleaseItem>> {
        val resItems = ArrayList<ReleaseItem>()
        val responseJson = JSONObject(responseText)
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

}
