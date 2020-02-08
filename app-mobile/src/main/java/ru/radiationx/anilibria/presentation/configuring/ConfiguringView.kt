package ru.radiationx.anilibria.presentation.configuring

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ConfiguringView : MvpView {
    fun updateScreen(screenState: ConfiguringPresenter.ScreenState)
}