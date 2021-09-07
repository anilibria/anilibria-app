package ru.radiationx.anilibria.ui.fragments.schedule

import android.view.View
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.feed.ScheduleItem

class ScheduleAdapter(
    scheduleClickListener: (ScheduleItem, View, Int) -> Unit,
    scrollListener: (Int) -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(FeedSectionDelegate({}))
        addDelegate(FeedSchedulesDelegate(scheduleClickListener, scrollListener))
    }

    fun bindItems(newItems: List<Pair<String, List<ScheduleItem>>>) {
        items = mutableListOf<ListItem>().apply {
            newItems.forEach { pair ->
                add(FeedSectionListItem(pair.first))
                add(FeedSchedulesListItem(pair.first, pair.second))
            }
        }
    }

    fun getPositionByDay(item: Pair<String, List<ScheduleItem>>): Int {
        return items.indexOfFirst {
            (it as? FeedSectionListItem)?.title == item.first
        }
    }
}