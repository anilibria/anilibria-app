package ru.radiationx.anilibria.data.repository

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.article.ArticleFull
import ru.radiationx.anilibria.data.api.models.article.ArticleItem
import ru.radiationx.anilibria.data.api.modules.ArticleApi

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleRepository(private val articleApi: ArticleApi) {

    fun getArticle(articleUrl: String): Single<ArticleFull> = Single.defer {
        articleApi.getArticle(articleUrl)
    }

    fun getArticles(category: String, subCategory: String, page: Int): Single<Paginated<List<ArticleItem>>> = Single.defer {
        articleApi.getArticles(category, subCategory, page)
    }
}
