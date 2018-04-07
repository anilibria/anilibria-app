package ru.radiationx.anilibria.model.data.remote.parsers

import com.mintrocket.gisdelivery.extension.nullGet
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleParser(private val apiUtils: IApiUtils) {

    /*
    * 1.    Int     Какой-то айдишник битриксовский
    * 2.    String  Относительная ссылка на статью
    * 3.    String  Заголовок статьи
    * 4.    Int     Id юзера
    * 5.    String  Ник юзера
    * 6.    String  Относительная ссылка на изображение
    * 7.    Int     Ширина изображения
    * 8.    Int     Высота изображения
    * 9.    String^ Текстовый контент
    * 10.   String? Ссылка на "ВСЕ ВЫПУСКИ"
    * 11.   Int^    Просмотры
    * 12.   Int^    Комментарии
    * */
    private val listPatternSource = "<div[^>]*?class=\"[^\"]*?news_block[^\"]*?\"[^>]*?id=\"bx_\\d+_(\\d+)\"[^>]*?>[\\s\\S]*?<h1[^>]*?class=\"[^\"]*?news-name[^\"]*?\"[^>]*?>[\\s\\S]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/h1>[\\s\\S]*?<span[^>]*?class=\"published\"[^>]*?>[\\s\\S]*?<a[^>]*?href=\"\\/user\\/(\\d+)\\/\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/span>[\\s\\S]*?<div[^>]*?class=\"[^\"]*?news-content[^\"]*?\"[^>]*?>[\\s\\S]*?<a[^>]*>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?width=\"(\\d+)\"[^>]*?height=\"(\\d+)\"[^>]*?>[^<]*?<\\/a>[^<]*?<span[^>]*?class=\"news-preview-text\"[^>]*?>([\\s\\S]*?)<\\/span>[^<]*?<div[^>]*?class=\"block_fix\"[^>]*>[^<]*?<\\/div>[\\s\\S]*?<div[^>]*?class=\"news_footer\"[^>]*?>[^<]*?(?:<a[^>]*?>[\\s\\S]*?<\\/a>[^<]*?)?(?:<a[^>]*?href=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<\\/a>)?[^<]*?<span[^>]*?>[^:]*?:\\s?([^<]*?)<\\/span>[^<]*?<span[^>]*?>[^:]*?:\\s?([^<]*?)<\\/span>"

    /*
    * 1.    Int     Текущая страница
    * 2.    Int     Последняя страница (всего)
    * */
    private val paginationPatternSource = "<div[^>]*?class=\"[^\"]*?bx_pagination_page[^\"]*?\"[^>]*?>[\\s\\S]*?<li[^>]*?class=\"bx_active\"[^>]*?>(\\d+)<\\/li>[\\s\\S]*?<li><a[^>]*?>(\\d+)<\\/a><\\/li>[^<]*?<li><a[^>]*?>&#8594;<\\/a>"

    /*
    * 1.    String  Заголовок
    * 2.    String  Контент
    * 3.    Int     Id юзера
    * 4.    String  Ник юзера
    * 5.    String  Дата
    * */
    private val fullArticlePatternSource = "<div[^>]*?class=\"[^\"]*?news-detail-header[^\"]*?\"[^>]*?>[^<]*?<h1[^>]*?>([\\s\\S]*?)<\\/h1>[^<]*?<\\/div>[\\s\\S]*?<div[^>]*?class=\"[^\"]*?news-detail-content[^\"]*?\"[^>]*?>([\\s\\S]*?)(?:<a[^>]*?id=\"back-to-list\"[^>]*?>[\\s\\S]*?<\\/a>[^<]*?)?<\\/div>[^<]*?<div[^>]*?class=\"[^\"]*?news-detail-footer[^\"]*?\"[^>]*?>[^<]*?<span[^>]*?>[\\s\\S]*?<a[^>]*?href=\"[^\"]*?(\\d+)[^\"]*?\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/span>[\\s\\S]*?<span[^>]*?>[^<]*?<i[^>]*?>([\\s\\S]*?)<\\/i>[\\s\\S]*?<\\/span>"

    private val listPattern: Pattern by lazy {
        Pattern.compile(listPatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val paginationPattern: Pattern by lazy {
        Pattern.compile(paginationPatternSource, Pattern.CASE_INSENSITIVE)
    }

    private fun safeNumber(value: String): String {
        val result = value.trim()
        return if (result.isEmpty()) "0" else result
    }

    fun articles2(httpResponse: String): Paginated<List<ArticleItem>> {
        val resItems = mutableListOf<ArticleItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val jsonItem = jsonItems.getJSONObject(i)
            resItems.add(ArticleItem().apply {
                id = jsonItem.getInt("id")
                code = jsonItem.getString("code")
                url = Api.BASE_URL + jsonItem.getString("url")
                title = jsonItem.getString("title")
                userId = jsonItem.getInt("userId")
                userNick = jsonItem.getString("userNick")
                imageUrl = Api.BASE_URL_IMAGES + jsonItem.getString("coverImage")
                imageWidth = jsonItem.getInt("coverImageWidth")
                imageHeight = jsonItem.getInt("coverImageHeight")
                content = jsonItem.getString("content")
                viewsCount = jsonItem.getInt("countViews")
                commentsCount = jsonItem.getInt("countComments")
                date = jsonItem.getString("date")
            })
        }

        val pagination = Paginated(resItems)
        val jsonNav = responseJson.getJSONObject("navigation")
        jsonNav.nullGet("total")?.let { pagination.total = it.toString().toInt() }
        jsonNav.nullGet("page")?.let { pagination.current = it.toString().toInt() }
        jsonNav.nullGet("total_pages")?.let { pagination.allPages = it.toString().toInt() }
        return pagination
    }

    /*fun article(httpResponse: String): ArticleFull {
        val result = ArticleFull()
        val matcher: Matcher = fullPattern.matcher(httpResponse)
        if (matcher.find()) {
            result.apply {
                title = apiUtils.escapeHtml(matcher.group(1)).orEmpty()
                content = matcher.group(2).trim()
                userId = matcher.group(3).toInt()
                userNick = apiUtils.escapeHtml(matcher.group(1)).orEmpty()
                date = matcher.group(5)
            }
        }
        return result
    }*/

    fun article2(httpResponse: String): ArticleItem {
        val responseJson = JSONObject(httpResponse)
        return ArticleItem().apply {
            id = responseJson.getInt("id")
            code = responseJson.getString("code")
            url = Api.BASE_URL + responseJson.getString("url")
            title = responseJson.getString("title")
            userId = responseJson.getInt("userId")
            userNick = responseJson.getString("userNick")
            imageUrl = Api.BASE_URL_IMAGES + responseJson.getString("coverImage")
            imageWidth = responseJson.getInt("coverImageWidth")
            imageHeight = responseJson.getInt("coverImageHeight")
            content = responseJson.getString("content")
            viewsCount = responseJson.getInt("countViews")
            commentsCount = responseJson.getInt("countComments")
            date = responseJson.getString("date")
        }
    }
}
