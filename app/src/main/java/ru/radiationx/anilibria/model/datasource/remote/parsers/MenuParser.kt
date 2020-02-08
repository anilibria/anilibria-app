package ru.radiationx.anilibria.model.datasource.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.extension.nullString
import javax.inject.Inject

class MenuParser @Inject constructor() {

    fun parse(responseJson: JSONArray): List<LinkMenuItem> {
        val result = mutableListOf<LinkMenuItem>()
        for (i in 0 until responseJson.length()) {
            responseJson.optJSONObject(i)?.let { addressJson ->
                result.add(parseItem(addressJson))
            }
        }
        return result
    }

    fun parseItem(jsonItem: JSONObject): LinkMenuItem = LinkMenuItem(
            jsonItem.getString("title"),
            jsonItem.nullString("absoluteLink"),
            jsonItem.getString("sitePagePath"),
            jsonItem.getString("icon")
    )

}