package ru.radiationx.anilibria.ui.fragments.release.list

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter(
    listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {


    init {
        addDelegate(ReleaseItemDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(state: ReleaseScreenState) {
        val newItems = mutableListOf<ListItem>()
        if (state.items.isEmpty() && !state.refreshing) {
            newItems.add(placeHolder)
        }
        newItems.addAll(state.items.map { ReleaseListItem(it) })
        if (state.hasMorePages) {
            newItems.add(LoadMoreListItem("bottom"))
        }
        items = newItems
    }

    interface ItemListener : LoadMoreDelegate.Listener, ReleaseItemDelegate.Listener
}
