package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.shared.ktx.android.nullGet
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