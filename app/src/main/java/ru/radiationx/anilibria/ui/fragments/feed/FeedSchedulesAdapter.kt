package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter

class FeedSchedulesAdapter(
        private val itemListener: (ReleaseItem) -> Unit
) : OptimizeAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        addDelegate(FeedScheduleDelegate(itemListener))
    }

    fun bindItems(newItems: List<ReleaseItem>) {
        items.clear()
        items.addAll(newItems.map { FeedScheduleListItem(it) })
        notifyDataSetChanged()
    }

}