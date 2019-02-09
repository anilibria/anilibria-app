package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class CheckerParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun parse(responseJson: JSONObject): UpdateData {
        val resData = UpdateData()
        val jsonUpdate = responseJson.getJSONObject("update")

        resData.code = jsonUpdate.optInt("version_code", Int.MAX_VALUE)
        resData.build = jsonUpdate.optInt("version_build", Int.MAX_VALUE)
        resData.name = jsonUpdate.optString("version_name")
        resData.date = jsonUpdate.optString("build_date")

        jsonUpdate.getJSONArray("links")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let { linkJson ->
                    resData.links.add(UpdateData.UpdateLink().apply {
                        name = linkJson.optString("name", "Unknown")
                        url = linkJson.optString("url", "")
                        type = linkJson.optString("type", "site")
                    })
                }
            }
        }

        jsonUpdate.getJSONArray("important")?.let {
            for (i in 0 until it.length()) {
                it.optString(i, null)?.let {
                    resData.important.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("added")?.let {
            for (i in 0 until it.length()) {
                it.optString(i, null)?.let {
                    resData.added.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("fixed")?.let {
            for (i in 0 until it.length()) {
                it.optString(i, null)?.let {
                    resData.fixed.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("changed")?.let {
            for (i in 0 until it.length()) {
                it.optString(i, null)?.let {
                    resData.changed.add(it)
                }
            }
        }

        return resData
    }
}