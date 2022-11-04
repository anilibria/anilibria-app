package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.mapStrings
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

class SearchParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig
) {

    fun fastSearch(jsonResponse: JSONArray): List<SuggestionItem> {
        return jsonResponse.mapObjects { jsonItem ->
            SuggestionItem(
                id = jsonItem.getInt("id"),
                code = jsonItem.getString("code"),
                names = jsonItem.getJSONArray("names").mapStrings {
                    apiUtils.escapeHtml(it).toString()
                },
                poster = "${apiConfig.baseImagesUrl}${jsonItem.nullString("poster")}"
            )
        }
    }

    fun years(jsonResponse: JSONArray): List<YearItem> {
        val result: MutableList<YearItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val yearText = jsonResponse.getString(i)
            val genreItem = YearItem(
                title = yearText,
                value = yearText
            )
            result.add(genreItem)
        }
        return result
    }

    fun genres(jsonResponse: JSONArray): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        for (i in 0 until jsonResponse.length()) {
            val genreText = jsonResponse.getString(i)
            val genreItem = GenreItem(
                title = genreText.capitalize(),
                value = genreText
            )
            result.add(genreItem)
        }
        return result
    }

}