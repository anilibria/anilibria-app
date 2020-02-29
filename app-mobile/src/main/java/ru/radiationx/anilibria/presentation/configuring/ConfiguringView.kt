package ru.radiationx.anilibria.presentation.configuring

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.common.ConfigScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface ConfiguringView : MvpView {

    fun updateScreen(screenState: ConfigScreenState)
}