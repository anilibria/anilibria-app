package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseRemindDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter

/**
 * Created by radiationx on 04.03.18.
 */
class SearchAdapter(
    listener: ReleasesAdapter.ItemListener,
    private val remindCloseListener: ReleaseRemindDelegate.Listener,
    private val placeholder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        delegatesManager.run {
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
            addDelegate(ReleaseItemDelegate(listener))
            addDelegate(LoadMoreDelegate(listener))
            addDelegate(PlaceholderDelegate())
        }
    }

    fun bindState(state: SearchScreenState) {
        val newItems = mutableListOf<ListItem>()
        if (state.items.isEmpty() && !state.refreshing && state.remindText != null) {
            newItems.add(ReleaseRemindListItem(state.remindText))
        }
        if (state.items.isEmpty() && !state.refreshing) {
            newItems.add(placeholder)
        }
        newItems.addAll(state.items.map { ReleaseListItem(it) })
        if (state.hasMorePages) {
            newItems.add(LoadMoreListItem("bottom"))
        }
        items = newItems
    }
}