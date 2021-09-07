package ru.radiationx.anilibria.ui.fragments.release.list

import android.util.Log
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import ru.radiationx.data.entity.app.release.ReleaseItem

/* Created by radiationx on 31.10.17. */

open class ReleasesAdapter(
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
        addDelegate(ReleaseItemDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    protected fun updatePlaceholder(condition: Boolean = items.isEmpty()) {
        if (condition) {
            items.add(placeHolder)
        } else {
            items.removeAll { it is PlaceholderListItem }
        }
    }

    private fun removeLoadMore() {
        this.items.removeAll { it is LoadMoreListItem }
    }

    protected fun addLoadMore() {
        if (endless) {
            this.items.add(LoadMoreListItem())
        }
    }

    fun insertMore(newItems: List<ReleaseItem>) {
        val prevItems = itemCount
        removeLoadMore()
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        addLoadMore()
        notifyItemRangeInserted(prevItems, itemCount)
    }

    open fun bindItems(newItems: List<ReleaseItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        updatePlaceholder()
        addLoadMore()
        notifyDataSetChanged()
    }

    fun removeItems(remItems: List<ReleaseItem>) {
        remItems.forEach { remItem ->
            val index = items.indexOfFirst { it is ReleaseListItem && it.item.id == remItem.id }
            if (index != -1) {
                items.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    fun updateItems(updItems: List<ReleaseItem>) {
        updItems.map { updItem -> items.indexOfFirst { it is ReleaseListItem && it.item.id == updItem.id } }
            .forEach {
                Log.e("lalalupdata", "adapter notify index $it")
                notifyItemChanged(it)
            }
    }

    interface ItemListener : LoadMoreDelegate.Listener, ReleaseItemDelegate.Listener
}
