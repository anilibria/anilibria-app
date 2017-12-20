package ru.radiationx.anilibria.ui.fragments.article

import android.support.v7.widget.DialogTitle
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 20.12.17.
 */
interface ArticleView : IBaseView {
    fun showArticle(article: ArticleFull)
    fun preShow(title: String, nick: String, comments: Int, views: Int)
}