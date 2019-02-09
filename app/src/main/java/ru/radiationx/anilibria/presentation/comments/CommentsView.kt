package ru.radiationx.anilibria.presentation.comments

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface CommentsView : IBaseView {

    @StateStrategyType(AddToEndStrategy::class)
    fun showComments(comments: List<Comment>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMoreComments(comments: List<Comment>)

    fun setEndlessComments(enable: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun onCommentSent()

    @StateStrategyType(SkipStrategy::class)
    fun addCommentText(text: String)
}