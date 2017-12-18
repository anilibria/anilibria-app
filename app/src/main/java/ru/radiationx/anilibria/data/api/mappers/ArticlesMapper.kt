package ru.radiationx.anilibria.data.api.mappers

import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.data.api.models.Paginated
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.12.17.
 */
object ArticlesMapper {

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
    * 11.   Int     Просмотры
    * 12.   Int     Комментарии
    * */
    private val listPatternSource = "<div[^>]*?class=\"[^\"]*?news_block[^\"]*?\"[^>]*?id=\"bx_\\d+_(\\d+)\"[^>]*?>[\\s\\S]*?<h1[^>]*?class=\"[^\"]*?news-name[^\"]*?\"[^>]*?>[\\s\\S]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/h1>[\\s\\S]*?<span[^>]*?class=\"published\"[^>]*?>[\\s\\S]*?<a[^>]*?href=\"\\/user\\/(\\d+)\\/\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/span>[\\s\\S]*?<div[^>]*?class=\"[^\"]*?news-content[^\"]*?\"[^>]*?>[\\s\\S]*?<a[^>]*>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?width=\"(\\d+)\"[^>]*?height=\"(\\d+)\"[^>]*?>[^<]*?<\\/a>[^<]*?<span[^>]*?class=\"news-preview-text\"[^>]*?>([\\s\\S]*?)<\\/span>[^<]*?<div[^>]*?class=\"block_fix\"[^>]*>[^<]*?<\\/div>[\\s\\S]*?<div[^>]*?class=\"news_footer\"[^>]*?>[^<]*?(?:<a[^>]*?>[\\s\\S]*?<\\/a>[^<]*?)?(?:<a[^>]*?href=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<\\/a>)?[^<]*?<span[^>]*?>[^<]*?(\\d+)[^<]*?<\\/span>[^<]*?<span[^>]*?>[^<]*?(\\d+)[^<]*?<\\/span>"

    /*
    * 1.    Int     Текущая страница
    * 2.    Int     Последняя страница (всего)
    * */
    private val paginationPatternSource = "<div[^>]*?class=\"[^\"]*?bx_pagination_page[^\"]*?\"[^>]*?>[\\s\\S]*?<li[^>]*?class=\"bx_active\"[^>]*?>(\\d+)<\\/li>[\\s\\S]*?<li><a[^>]*?>(\\d+)<\\/a><\\/li>[^<]*?<li><a[^>]*?>&#8594;<\\/a>"

    val listPattern: Pattern by lazy {
        Pattern.compile(listPatternSource, Pattern.CASE_INSENSITIVE)
    }

    val paginationPattern: Pattern by lazy {
        Pattern.compile(paginationPatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun articles(httpResponse: String): Paginated<List<ArticleItem>> {
        val items = mutableListOf<ArticleItem>()
        val matcher: Matcher = listPattern.matcher(httpResponse)
        while (matcher.find()) {
            items.add(ArticleItem().apply {
                elementId = matcher.group(1).toInt()
                url = matcher.group(2)
                title = matcher.group(3)
                userId = matcher.group(4).toInt()
                userNick = matcher.group(5)
                imageUrl = Api.Companion.BASE_URL +matcher.group(6)
                imageWidth = matcher.group(7).toInt()
                imageHeight = matcher.group(8).toInt()
                content = matcher.group(9).trim()
                otherUrl = Api.Companion.BASE_URL + matcher.group(10)
                viewsCount = matcher.group(11).toInt()
                commentsCount = matcher.group(12).toInt()
            })
        }
        val result = Paginated(items)

        val paginationMatcher = paginationPattern.matcher(httpResponse)
        if (paginationMatcher.find()) {
            result.current = paginationMatcher.group(1).toInt()
            result.allPages = paginationMatcher.group(2).toInt()
            result.itemsPerPage = 6
        }

        return result
    }

    fun article(httpResponse: String): ArticleFull {

        return ArticleFull()
    }
}