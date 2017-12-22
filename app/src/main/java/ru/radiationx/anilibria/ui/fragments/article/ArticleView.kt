package ru.radiationx.anilibria.ui.fragments.article

import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 20.12.17.
 */
interface ArticleView : IBaseView {
    fun showArticle(article: ArticleFull)
    fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int)
}
