package ru.radiationx.anilibria.presentation.search

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface FastSearchView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: FastSearchScreenState)
}