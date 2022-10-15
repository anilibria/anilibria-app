package ru.radiationx.anilibria.ui.fragments.schedule

import android.view.View
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class ScheduleAdapter(
    scheduleClickListener: (ScheduleItemState, View, Int) -> Unit,
    scrollListener: (Int) -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(FeedSectionDelegate({}))
        addDelegate(FeedSchedulesDelegate(scheduleClickListener, scrollListener))
    }

    fun bindState(state: ScheduleScreenState) {
        val newItems = mutableListOf<ListItem>()
        state.dayItems.forEach { dayItem ->
            newItems.add(FeedSectionListItem(dayItem.title, dayItem.title, null, null))
            newItems.add(FeedSchedulesListItem(dayItem.title, dayItem.items))
        }
        items = newItems
    }

    fun getPositionByDay(day: ScheduleDayState): Int = items.indexOfFirst {
        (it as? FeedSectionListItem)?.title == day.title
    }
}