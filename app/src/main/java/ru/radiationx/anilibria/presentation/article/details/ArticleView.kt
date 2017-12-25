package ru.radiationx.anilibria.presentation.article.details

import ru.radiationx.anilibria.entity.app.article.ArticleFull
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 20.12.17.
 */
interface ArticleView : IBaseView {
    fun showArticle(article: ArticleFull)
    fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int)
}
