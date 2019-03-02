package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.extension.nullGet
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

class YoutubeParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun parse(jsonResponse: JSONObject): Paginated<List<YoutubeItem>> {
        val result = mutableListOf<YoutubeItem>()
        val jsonItems = jsonResponse.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            val item = YoutubeItem()
            item.id = jsonItem.getInt("id")
            item.title = apiUtils.escapeHtml(jsonItem.nullString("title"))
            item.image = "${Api.BASE_URL_IMAGES}${jsonItem.nullString("image")}"
            item.vid = jsonItem.nullString("vid")
            item.views = jsonItem.getInt("views")
            item.comments = jsonItem.getInt("comments")
            item.timestamp = jsonItem.getInt("timestamp")
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