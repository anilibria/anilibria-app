package ru.radiationx.anilibria.presentation.schedule

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.feed.ScheduleItem

@StateStrategyType(AddToEndSingleStrategy::class)
interface ScheduleView : IBaseView {
    fun showSchedules(items: List<Pair<String, List<ScheduleItem>>>)

    @StateStrategyType(SkipStrategy::class)
    fun scrollToDay(item:Pair<String, List<ScheduleItem>>)
}