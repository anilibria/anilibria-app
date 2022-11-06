package ru.radiationx.anilibria.presentation.comments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface VkCommentsView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: VkCommentsScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun pageReloadAction()
}