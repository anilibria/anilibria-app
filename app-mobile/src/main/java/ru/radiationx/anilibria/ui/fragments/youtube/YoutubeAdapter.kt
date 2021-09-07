package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.youtube.YoutubeItem

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    var listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {


    private var localItems = mutableListOf<ListItem>()

    var endless: Boolean = false
        set(enable) {
            field = enable
            removeLoadMore()
            addLoadMore()
        }

    init {
        items = mutableListOf()
        addDelegate(YoutubeDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    private fun removeLoadMore() {
        localItems.removeAll { it is LoadMoreListItem }
    }

    private fun addLoadMore() {
        if (endless) {
            localItems.add(LoadMoreListItem("bottom"))
        }
    }

    fun insertMore(newItems: List<YoutubeItem>) {
        removeLoadMore()
        localItems.addAll(newItems.map { YoutubeListItem(it) })
        addLoadMore()
        notifyDiffItems()
    }

    fun bindItems(newItems: List<YoutubeItem>) {
        localItems.clear()
        localItems.addAll(newItems.map { YoutubeListItem(it) })
        if (items.isEmpty()) {
            localItems.add(placeHolder)
        } else {
            localItems.removeAll { it is PlaceholderListItem }
        }
        addLoadMore()
        notifyDiffItems()
    }

    private fun notifyDiffItems() {
        items = localItems.toList()
    }

    interface ItemListener : LoadMoreDelegate.Listener, YoutubeDelegate.Listener

}
