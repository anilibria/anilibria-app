package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
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
        apiUtils: IApiUtils
) {

    private val articleParser = ArticleParser(apiUtils)
    private val releaseParser = ReleaseParser(apiUtils)

    fun getArticle(code: String): Single<ArticleItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "article",
                "code" to code
        )
        return client
                .get(Api.API_V2_URL, args)
                .map { articleParser.article2(it) }
    }

    fun getArticles(category: String, page: Int): Single<Paginated<List<ArticleItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "articles",
                "PAGEN_1" to page.toString(),
                "SIZEN_1" to "6",
                "section" to category
        )

        return client
                .get(Api.API_V2_URL, args)
                .map { articleParser.articles2(it) }
    }

    fun getComments(id: Int, page: Int): Single<Paginated<List<Comment>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "comments",
                "id" to id.toString(),
                "from" to "article",
                "PAGEN_1" to page.toString()
        )
        return client
                .get(Api.API_V2_URL, args)
                .map { releaseParser.comments(it) }
    }

}
