package ru.radiationx.anilibria.presentation.schedule

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface ScheduleView : IBaseView {
    fun showSchedules(items: List<Pair<String, List<ScheduleItem>>>)

    @StateStrategyType(SkipStrategy::class)
    fun scrollToDay(item:Pair<String, List<ScheduleItem>>)
}