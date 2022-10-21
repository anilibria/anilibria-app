package ru.radiationx.anilibria.presentation.teams

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


interface TeamsView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(data: List<TeamState>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setLoading(isLoading: Boolean)

}