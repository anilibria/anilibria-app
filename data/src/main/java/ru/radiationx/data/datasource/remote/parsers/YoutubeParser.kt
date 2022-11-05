package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

class YoutubeParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig,
    private val paginationParser: PaginationParser
) {

    fun youtube(jsonItem: JSONObject): YoutubeItem {
        return YoutubeItem(
            id = jsonItem.getInt("id"),
            title = apiUtils.escapeHtml(jsonItem.nullString("title")),
            image = "${apiConfig.baseImagesUrl}${jsonItem.nullString("image")}",
            vid = jsonItem.nullString("vid"),
            views = jsonItem.getInt("views"),
            comments = jsonItem.getInt("comments"),
            timestamp = jsonItem.getInt("timestamp")
        )
    }

    fun parse(jsonResponse: JSONObject): Paginated<YoutubeItem> {
        return paginationParser.parse(jsonResponse) {
            youtube(it)
        }
    }
}