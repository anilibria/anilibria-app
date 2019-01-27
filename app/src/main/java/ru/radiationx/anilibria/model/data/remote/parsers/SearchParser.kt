package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class SearchParser(private val apiUtils: IApiUtils) {

    private val fastSearchPatternSource = "<a[^>]*?href=\"[^\"]*?\\/release\\/[^\"]*?\"[^>]*?>[^<]*?<img[^>]*?>([\\s\\S]*?) \\/ ([\\s\\S]*?)(?:<\\/a>[^>]*?)?<\\/td>"

    private val fastSearchPattern: Pattern by lazy {
        Pattern.compile(fastSearchPatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun fastSearch(httpResponse: String): List<SearchItem> {
        val result: MutableList<SearchItem> = mutableListOf()
        val matcher: Matcher = fastSearchPattern.matcher(httpResponse)
        while (matcher.find()) {
            result.add(SearchItem().apply {
                originalTitle = matcher.group(1)
                title = matcher.group(2)
            })
        }
        return result
    }

    fun genres(httpResponse: String): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        val jsonItems = JSONObject(httpResponse).getJSONArray("data")
        for (i in 0 until jsonItems.length()) {
            val genreText = jsonItems.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.substring(0, 1).toUpperCase() + genreText.substring(1)
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

}