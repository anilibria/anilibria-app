package ru.radiationx.anilibria.presentation.article.details

import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 20.12.17.
 */
interface ArticleView : IBaseView {
    fun showArticle(article: ArticleItem)
    fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int)

    @StateStrategyType(AddToEndStrategy::class)
    fun showComments(comments: List<Comment>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMoreComments(comments: List<Comment>)

    fun setEndlessComments(enable: Boolean)
}
