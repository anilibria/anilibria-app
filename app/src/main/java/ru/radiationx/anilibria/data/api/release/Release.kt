package ru.radiationx.anilibria.data.api.release;

import android.text.Html
import io.reactivex.Observable
import org.json.JSONObject
import ru.radiationx.anilibria.data.Client
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.releases.ReleaseItem

/* Created by radiationx on 05.11.17. */

class Release {

    fun getRelease(id: Int): Observable<ReleaseItem> {
        return Observable.fromCallable {
            val url = "http://www.anilibria.tv/api/api.php?action=release&ELEMENT_ID=" + id
            val response = Client.instance.get(url)
            parseRelease(response)
        }
    }

    @Throws(Exception::class)
    private fun parseRelease(responseText: String?): ReleaseItem {
        val release = ReleaseItem()

        val responseJson = JSONObject(responseText)
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
        release.description = Html.fromHtml(responseJson.getString("description")).toString()

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

        return release
    }
}
