package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.extension.nullGet
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

class FeedParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun feed(
            jsonResponse: JSONArray,
            releaseParser: ReleaseParser,
            youtubeParser: YoutubeParser
    ): List<FeedItem> {
        val result = mutableListOf<FeedItem>()
        for (i in 0 until jsonResponse.length()) {
            val jsonItem = jsonResponse.getJSONObject(i)
            val jsonRelease = jsonItem.nullGet("release") as JSONObject?
            val jsonYoutube = jsonItem.nullGet("youtube") as JSONObject?
            val item = when {
                jsonRelease != null -> {
                    FeedItem(release = releaseParser.parseRelease(jsonRelease))
                }
                jsonYoutube != null -> {
                    FeedItem(youtube = youtubeParser.youtube(jsonYoutube))
                }
                else -> null
            }
            if (item != null) {
                result.add(item)
            }
        }
        return result
    }
}