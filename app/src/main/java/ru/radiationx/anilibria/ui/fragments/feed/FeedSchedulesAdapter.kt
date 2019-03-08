package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter

class FeedSchedulesAdapter(
        private val clickListener: (FeedScheduleItem, View) -> Unit
) : OptimizeAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        addDelegate(FeedScheduleDelegate(clickListener))
    }

    fun bindItems(newItems: List<FeedScheduleItem>) {
        items.clear()
        items.addAll(newItems.map { FeedScheduleListItem(it) })
        notifyDataSetChanged()
    }

}