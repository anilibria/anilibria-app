package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.extension.nullString
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import javax.inject.Inject

class SearchParser @Inject constructor(
        private val apiUtils: IApiUtils,
        private val apiConfig: ApiConfig
) {

    fun fastSearch(jsonResponse: JSONArray): List<SuggestionItem> {
        val result: MutableList<SuggestionItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val jsonItem = jsonResponse.getJSONObject(i)
            val item = SuggestionItem()

            item.id = jsonItem.getInt("id")
            item.code = jsonItem.getString("code")
            item.names.addAll(jsonItem.getJSONArray("names").let { names ->
                (0 until names.length()).map {
                    apiUtils.escapeHtml(names.getString(it)).toString()
                }
            })
            item.poster = "${apiConfig.baseImagesUrl}${jsonItem.nullString("poster")}"
            result.add(item)
        }
        return result
    }

    fun years(jsonResponse: JSONArray): List<YearItem> {
        val result: MutableList<YearItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val yearText = jsonResponse.getString(i)
            val genreItem = YearItem().apply {
                title = yearText
                value = yearText
            }
            result.add(genreItem)
        }
        return result
    }

    fun genres(jsonResponse: JSONArray): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val genreText = jsonResponse.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.capitalize()
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

}