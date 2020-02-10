package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import ru.radiationx.data.entity.app.feed.ScheduleItem

class FeedSchedulesAdapter(
        private val clickListener: (ScheduleItem, View) -> Unit
) : OptimizeAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        addDelegate(FeedScheduleDelegate(clickListener))
    }

    fun bindItems(newItems: List<ScheduleItem>) {
        items.clear()
        items.addAll(newItems.map { FeedScheduleListItem(it) })
        notifyDataSetChanged()
    }

}