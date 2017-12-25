package ru.radiationx.anilibria.data.api.modules

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.article.ArticleFull
import ru.radiationx.anilibria.data.api.models.article.ArticleItem
import ru.radiationx.anilibria.data.api.parsers.ArticleParser
import ru.radiationx.anilibria.data.client.IClient

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleApi(private val client: IClient) {

    fun getArticle(articleUrl: String): Single<ArticleFull> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}$articleUrl"
        return client.get(url, args)
                .map { ArticleParser.article(it) }
    }

    fun getArticles(category: String, subCategory: String, page: Int): Single<Paginated<List<ArticleItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "PAGEN_1" to page.toString(),
                "SIZEN_1" to "6"
        )
        var url = Api.BASE_URL
        if (subCategory.isNotEmpty()) {
            url += "$subCategory/"
        } else if (category.isNotEmpty()) {
            url += "$category/"
        }

        return client.get(url, args)
                .map { ArticleParser.articles(it) }
    }

}
