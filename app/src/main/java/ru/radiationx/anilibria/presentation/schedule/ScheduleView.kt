package ru.radiationx.anilibria.presentation.schedule

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface ScheduleView : IBaseView {
    fun showSchedules(items: List<Pair<String, List<FeedScheduleItem>>>)

    @StateStrategyType(SkipStrategy::class)
    fun scrollToDay(item:Pair<String, List<FeedScheduleItem>>)
}