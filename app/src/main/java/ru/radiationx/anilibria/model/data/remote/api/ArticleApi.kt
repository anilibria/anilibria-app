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
                .get(Api.API_URL, args)
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
                .get(Api.API_URL, args)
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
                .get(Api.API_URL, args)
                .map { releaseParser.comments(it) }
    }

    fun sendComment(url: String, id: Int, text: String, sessId: String): Single<Paginated<List<Comment>>> {
        val args = mapOf(
                //"index" to "ZZtH",
                "back_page" to "/api/api_v2.php?action=comments&id=$id&from=article&PAGEN_1=1",
                "ELEMENT_ID" to "$id",
                "SECTION_ID" to "",
                "save_product_review" to "Y",
                "preview_comment" to "N",
                "sessid" to sessId,
                //"autosave_id" to "27f64ba01ec10848c3c2cec1e137d06ac",
                "REVIEW_TEXT" to text,
                "REVIEW_USE_SMILES" to "Y"
        )

        return client.post(url, args)
                .map { releaseParser.comments(it) }
    }

}
