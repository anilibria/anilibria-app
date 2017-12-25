package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.article.ArticleFull
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.model.data.remote.api.ArticleApi

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
