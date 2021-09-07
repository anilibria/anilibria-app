package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import ru.radiationx.data.entity.app.youtube.YoutubeItem

/* Created by radiationx on 31.10.17. */

class YoutubeAdapter(
    var listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter()  {


    var endless: Boolean = false
        set(enable) {
            field = enable
            removeLoadMore()
            addLoadMore()
            notifyDataSetChanged()
        }

    init {
        items = mutableListOf()
        addDelegate(YoutubeDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    private fun updatePlaceholder(condition: Boolean = items.isEmpty()) {
        if (condition) {
            items.add(placeHolder)
        } else {
            items.removeAll { it is PlaceholderListItem }
        }
    }

    private fun removeLoadMore() {
        this.items.removeAll { it is LoadMoreListItem }
    }

    private fun addLoadMore() {
        if (endless) {
            this.items.add(LoadMoreListItem())
        }
    }

    fun insertMore(newItems: List<YoutubeItem>) {
        val prevItems = itemCount
        removeLoadMore()
        this.items.addAll(newItems.map { YoutubeListItem(it) })
        addLoadMore()
        notifyItemRangeInserted(prevItems, itemCount)
    }

    fun bindItems(newItems: List<YoutubeItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { YoutubeListItem(it) })
        updatePlaceholder()
        addLoadMore()
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener, YoutubeDelegate.Listener

}
