package ru.radiationx.anilibria.data.repository

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.data.api.models.Paginated

/**
 * Created by radiationx on 18.12.17.
 */
class ArticlesRepository(private val api: Api) {

    fun getArticle(articleId: String): Single<ArticleFull> = Single.defer {
        api.getArticle(articleId)
    }

    fun getArticles(name: String, page: Int): Single<Paginated<List<ArticleItem>>> = Single.defer {
        api.getArticles(name, page)
    }
}
