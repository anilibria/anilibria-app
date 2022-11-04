package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

class YoutubeParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig,
    private val paginationParser: PaginationParser
) {

    fun youtube(jsonItem: JSONObject): YoutubeItem {
        val item = YoutubeItem()
        item.id = jsonItem.getInt("id")
        item.title = apiUtils.escapeHtml(jsonItem.nullString("title"))
        item.image = "${apiConfig.baseImagesUrl}${jsonItem.nullString("image")}"
        item.vid = jsonItem.nullString("vid")
        item.views = jsonItem.getInt("views")
        item.comments = jsonItem.getInt("comments")
        item.timestamp = jsonItem.getInt("timestamp")
        return item
    }

    fun parse(jsonResponse: JSONObject): Paginated<List<YoutubeItem>> {
        return paginationParser.parse(jsonResponse) { jsonItems ->
            jsonItems.mapObjects {
                youtube(it)
            }
        }
    }
}