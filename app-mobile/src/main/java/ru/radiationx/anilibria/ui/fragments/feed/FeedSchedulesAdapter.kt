package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.extension.setAndAwaitItems
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedScheduleDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class FeedSchedulesAdapter(
    clickListener: (ScheduleItemState, View, Int) -> Unit,
    longClickListener: (ScheduleItemState) -> Unit,
) : ListItemAdapter() {

    init {
        addDelegate(FeedScheduleDelegate(clickListener, longClickListener))
    }

    suspend fun bindItems(newItems: List<ScheduleItemState>) {
        setAndAwaitItems(newItems.map { FeedScheduleListItem(it) })
    }
}