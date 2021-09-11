package ru.radiationx.anilibria.presentation.youtube

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface YoutubeView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: YoutubeScreenState)
}