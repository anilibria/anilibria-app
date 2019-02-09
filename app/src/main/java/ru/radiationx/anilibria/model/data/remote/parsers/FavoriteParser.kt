package ru.radiationx.anilibria.model.data.remote.parsers

import android.util.Log
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.FavoriteData
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern
import javax.inject.Inject

class FavoriteParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    private val idNamePatternSource = "\\/release\\/([\\s\\S]*?)\\.html"
    private val idNamePattern: Pattern by lazy {
        Pattern.compile(idNamePatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun favorites2(httpResponse: String): FavoriteData {
        val resItems = mutableListOf<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = ReleaseItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")

            val matcher = idNamePattern.matcher(jsonItem.getString("link"))
            if (matcher.find()) {
                item.code = matcher.group(1)
            }

            item.description = jsonItem.getString("description")
            item.poster = Api.BASE_URL_IMAGES + jsonItem.get("image")

            val titles = jsonItem.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            /*if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }*/
            resItems.add(item)
        }
        val result = FavoriteData()
        result.sessId = responseJson.getString("sessId")
        result.items = Paginated(resItems)
        return result
    }

    fun favXhr(httpResponse: String): Int {
        Log.e("S_DEF_LOG", "favXhr " + httpResponse)
        return JSONObject(httpResponse).getInt("COUNT")
    }
}