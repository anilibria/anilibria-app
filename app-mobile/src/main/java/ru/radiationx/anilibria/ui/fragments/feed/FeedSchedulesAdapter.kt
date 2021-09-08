package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class FeedSchedulesAdapter(
    clickListener: (ScheduleItemState, View, Int) -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(FeedScheduleDelegate(clickListener))
    }

    fun bindItems(newItems: List<ScheduleItemState>) {
        items = newItems.map { FeedScheduleListItem(it) }
    }
}