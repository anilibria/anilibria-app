package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.feed.ScheduleItem

class FeedSchedulesAdapter(
    clickListener: (ScheduleItem, View, Int) -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(FeedScheduleDelegate(clickListener))
    }

    fun bindItems(newItems: List<ScheduleItem>) {
        items = newItems.map { FeedScheduleListItem(it) }
    }
}