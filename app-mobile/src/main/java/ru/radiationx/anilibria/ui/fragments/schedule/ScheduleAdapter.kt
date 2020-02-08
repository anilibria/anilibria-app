package ru.radiationx.anilibria.ui.fragments.schedule

import android.view.View
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter

class ScheduleAdapter(
        scheduleClickListener: (ScheduleItem, View) -> Unit
) : OptimizeAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        addDelegate(FeedSectionDelegate({}))
        addDelegate(FeedSchedulesDelegate(scheduleClickListener))
    }

    fun bindItems(newItems: List<Pair<String, List<ScheduleItem>>>) {
        items.clear()
        newItems.forEach { pair ->
            items.add(FeedSectionListItem(pair.first))
            items.add(FeedSchedulesListItem(pair.second))
        }
        notifyDataSetChanged()
    }

    fun getPositionByDay(item: Pair<String, List<ScheduleItem>>): Int {
        return items.indexOfFirst {
            (it as? FeedSectionListItem)?.title == item.first
        }
    }
}