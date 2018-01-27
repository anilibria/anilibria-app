package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.IApiUtils

/**
 * Created by radiationx on 27.01.18.
 */
class VitalParser(private val apiUtils: IApiUtils) {

    fun vital(httpResponse: String): List<VitalItem> {
        val resItems = mutableListOf<VitalItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = VitalItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.optInt("id", -1)
            item.name = jsonItem.optString("name", null)

            getType(jsonItem.getString("type"))?.let {
                item.type = it
            }

            getContentType(jsonItem.getString("contentType"))?.let {
                item.contentType = it
            }

            item.contentText = jsonItem.optString("contentText", null)
            item.contentImage = jsonItem.optString("contentImage", null)
            item.contentLink = jsonItem.optString("contentLink", null)

            val jsonRules = jsonItem.getJSONArray("rules")
            for (j in 0 until jsonRules.length()) {
                getRule(jsonRules.getString(j))?.let {
                    item.rules.add(it)
                }
            }

            val jsonEvents = jsonItem.getJSONArray("events")
            for (j in 0 until jsonEvents.length()) {
                getEvent(jsonEvents.getString(j))?.let {
                    item.events.add(it)
                }
            }
            resItems.add(item)
        }
        return resItems
    }

    private fun getType(jsonString: String): VitalItem.VitalType? = when (jsonString) {
        "banner" -> VitalItem.VitalType.BANNER
        "fullscreen" -> VitalItem.VitalType.FULLSCREEN
        "item" -> VitalItem.VitalType.CONTENT_ITEM
        else -> null
    }

    private fun getContentType(jsonString: String): VitalItem.ContentType? = when (jsonString) {
        "web" -> VitalItem.ContentType.WEB
        "image" -> VitalItem.ContentType.IMAGE
        else -> null
    }

    private fun getRule(jsonString: String): VitalItem.Rule? = when (jsonString) {
        "releaseDetail" -> VitalItem.Rule.RELEASE_DETAIL
        "releaseList" -> VitalItem.Rule.RELEASE_LIST
        "articleDetail" -> VitalItem.Rule.ARTICLE_DETAIL
        "articleList" -> VitalItem.Rule.ARTICLE_LIST
        "videoPlayer" -> VitalItem.Rule.VIDEO_PLAYER
        else -> null
    }

    private fun getEvent(jsonString: String): VitalItem.EVENT? = when (jsonString) {
        "exitVideo" -> VitalItem.EVENT.EXIT_VIDEO
        else -> null
    }
}