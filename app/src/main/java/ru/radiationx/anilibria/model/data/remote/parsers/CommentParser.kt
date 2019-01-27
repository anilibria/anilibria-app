package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.extension.nullGet
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils

class CommentParser(private val apiUtils: IApiUtils) {

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
}