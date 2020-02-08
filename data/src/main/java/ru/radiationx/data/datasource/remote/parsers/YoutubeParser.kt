package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.extension.nullGet
import ru.radiationx.data.extension.nullString
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import javax.inject.Inject

class YoutubeParser @Inject constructor(
        private val apiUtils: IApiUtils,
        private val apiConfig: ApiConfig
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
        val result = mutableListOf<YoutubeItem>()
        val jsonItems = jsonResponse.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            val item = youtube(jsonItem)
            result.add(item)
        }

        val pagination = Paginated(result)
        val jsonNav = jsonResponse.getJSONObject("pagination")
        jsonNav.nullGet("page")?.let { pagination.page = it.toString().toInt() }
        jsonNav.nullGet("perPage")?.let { pagination.perPage = it.toString().toInt() }
        jsonNav.nullGet("allPages")?.let { pagination.allPages = it.toString().toInt() }
        jsonNav.nullGet("allItems")?.let { pagination.allItems = it.toString().toInt() }
        return pagination
    }
}