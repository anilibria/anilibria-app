package ru.radiationx.anilibria.presentation.feed

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem

/* Created by radiationx on 16.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FeedView : IBaseView {

    fun showSchedules(title: String, items: List<ScheduleItem>)

    fun showRefreshProgress(show: Boolean)
    fun showEmptyProgress(show: Boolean)
    fun showPageProgress(show: Boolean)
    fun showEmptyView(show: Boolean)
    fun showEmptyError(show: Boolean, message: String?)
    fun showProjects(show: Boolean, items: List<FeedItem>)
    fun updateItems(items: List<FeedItem>)

}
