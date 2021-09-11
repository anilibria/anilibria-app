package ru.radiationx.anilibria.presentation.history

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.history.HistoryScreenState

/**
 * Created by radiationx on 18.02.18.
 */
interface HistoryView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: HistoryScreenState)
}