package ru.radiationx.anilibria.common

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.R
import ru.radiationx.data.datasource.remote.parsers.*
import toothpick.InjectConstructor

@InjectConstructor
class MockData(
    private val context: Context,
    private val feedParser: FeedParser,
    private val searchParser: SearchParser,
    private val releaseParser: ReleaseParser,
    private val scheduleParser: ScheduleParser,
    private val youtubeParser: YoutubeParser
) {

    private val feedSrc by lazy { getFromRaw(R.raw.alib_feed) }
    private val genresSrc by lazy { getFromRaw(R.raw.alib_genres) }
    private val releaseListSrc by lazy { getFromRaw(R.raw.alib_release_list) }
    private val scheduleSrc by lazy { getFromRaw(R.raw.alib_schedule) }
    private val youtubeSrc by lazy { getFromRaw(R.raw.alib_youtube) }

    val feed by lazy { feedParser.feed(JSONArray(feedSrc), releaseParser, youtubeParser) }
    val genres by lazy { searchParser.genres(JSONArray(genresSrc)) }
    val releases by lazy { releaseParser.releases(JSONObject(releaseListSrc)).data }
    val schedule by lazy { scheduleParser.schedule(JSONArray(scheduleSrc), releaseParser) }
    val youtube by lazy { youtubeParser.parse(JSONObject(youtubeSrc)).data }

    private fun getFromRaw(@RawRes id: Int): String = context.resources.openRawResource(id)
        .bufferedReader()
        .use {
            it.readText()
        }
        .also {
            Log.e("lalala", "readed $it")
        }
}