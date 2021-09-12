package ru.radiationx.anilibria.presentation.comments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsScreenState
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsState
import ru.radiationx.data.entity.app.page.VkComments

@StateStrategyType(AddToEndSingleStrategy::class)
interface VkCommentsView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: VkCommentsScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun pageReloadAction()
}