package ru.radiationx.anilibria.model.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.datasource.remote.IApiUtils
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class VitalParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun vital(httpResponse: String): List<VitalItem> {
        val resItems = mutableListOf<VitalItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            if (!jsonItem.optBoolean("active", false)) {
                continue
            }
            val item = VitalItem()
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

            jsonItem.optJSONArray("rules")?.let {
                for (j in 0 until it.length()) {
                    getRule(it.getString(j))?.let {
                        item.rules.add(it)
                    }
                }
            }

            jsonItem.optJSONArray("events")?.let {
                for (j in 0 until it.length()) {
                    getEvent(it.getString(j))?.let {
                        item.events.add(it)
                    }
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
        "videoPlayer" -> VitalItem.Rule.VIDEO_PLAYER
        else -> null
    }

    private fun getEvent(jsonString: String): VitalItem.EVENT? = when (jsonString) {
        "exitVideo" -> VitalItem.EVENT.EXIT_VIDEO
        else -> null
    }
}