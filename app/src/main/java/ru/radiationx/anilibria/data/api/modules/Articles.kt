package ru.radiationx.anilibria.data.api.modules

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.mappers.ArticlesMapper
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.client.IClient

/**
 * Created by radiationx on 18.12.17.
 */
class Articles(private val client: IClient) {

    fun getArticle(articleId: String): Single<ArticleFull> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = Api.BASE_URL + articleId
        return client.get(url, args)
                .map { ArticlesMapper.article(it) }
    }

    fun getArticles(page: Int): Single<Paginated<List<ArticleItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "PAGEN_1" to page.toString(),
                "SIZEN_1" to "6"
        )
        return client.get(Api.ARTICLES_URL, args)
                .map { ArticlesMapper.articles(it) }
    }

}