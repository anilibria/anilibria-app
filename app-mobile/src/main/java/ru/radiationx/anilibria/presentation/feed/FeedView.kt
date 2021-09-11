package ru.radiationx.anilibria.presentation.feed

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.feed.FeedScreenState

/* Created by radiationx on 16.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FeedView : MvpView {
    fun showState(state: FeedScreenState)
}
