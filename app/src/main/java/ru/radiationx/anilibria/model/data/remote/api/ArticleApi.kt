package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.article.ArticleFull
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ArticleParser
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleApi(
        private val client: IClient,
        private val apiUtils: IApiUtils
) {

    private val articleParser = ArticleParser(apiUtils)
    private val releaseParser = ReleaseParser(apiUtils)

    fun getArticle(articleUrl: String): Single<ArticleFull> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}$articleUrl"
        return client.get(url, args).map { articleParser.article(it) }
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

        return client.get(url, args).map { articleParser.articles(it) }
    }

    fun getComments(id: Int, page: Int): Single<Paginated<List<Comment>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "comments",
                "ELEMENT_ID" to id.toString(),
                "PAGEN_1" to page.toString()
        )
        return client.get("https://www.anilibria.tv/api/api_v2.php", args)
                .map { releaseParser.comments(it) }
    }

}
