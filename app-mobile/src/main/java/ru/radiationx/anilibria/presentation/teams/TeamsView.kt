package ru.radiationx.anilibria.presentation.teams

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.domain.team.Teams


interface TeamsView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showData(data: Teams)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setLoading(isLoading: Boolean)

}