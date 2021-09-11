package ru.radiationx.anilibria.presentation.schedule

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleDayState
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleScreenState

@StateStrategyType(AddToEndSingleStrategy::class)
interface ScheduleView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: ScheduleScreenState)

    @StateStrategyType(SkipStrategy::class)
    fun scrollToDay(day: ScheduleDayState)
}