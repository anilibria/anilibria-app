package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONArray
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class SearchParser(private val apiUtils: IApiUtils) {

    fun fastSearch(jsonResponse: JSONArray): List<SearchItem> {
        val result: MutableList<SearchItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val jsonItem = jsonResponse.getJSONObject(i)
            val item = SearchItem()

            item.id = jsonItem.getInt("id")
            item.code = jsonItem.getString("code")
            item.names.addAll(jsonItem.getJSONArray("names").let { names ->
                (0 until names.length()).map {
                    apiUtils.escapeHtml(names.getString(it)).toString()
                }
            })
            item.poster = Api.BASE_URL_IMAGES + jsonItem.nullString("poster")
            result.add(item)
        }
        return result
    }

    fun genres(jsonResponse: JSONArray): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val genreText = jsonResponse.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.substring(0, 1).toUpperCase() + genreText.substring(1)
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

}