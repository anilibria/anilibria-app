package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    private val listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {

    init {
        addDelegate(YoutubeDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(state: YoutubeScreenState) {
        val newItems = mutableListOf<ListItem>()
        if (state.items.isEmpty() && !state.refreshing) {
            newItems.add(placeHolder)
        }
        newItems.addAll(state.items.map { YoutubeListItem(it) })
        if (state.hasMorePages) {
            newItems.add(LoadMoreListItem("bottom"))
        }
        items = newItems
    }

    interface ItemListener : LoadMoreDelegate.Listener, YoutubeDelegate.Listener

}
