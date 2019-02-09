package ru.radiationx.anilibria.presentation.article.details

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.presentation.common.IBaseView

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

    @StateStrategyType(SkipStrategy::class)
    fun onCommentSent()

    @StateStrategyType(SkipStrategy::class)
    fun addCommentText(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun share(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun copyLink(url: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setCommentsRefreshing(isRefreshing: Boolean)
}
