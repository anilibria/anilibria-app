package ru.radiationx.anilibria.ui.fragments.release.list

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter(var listener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    companion object {
        private val RELEASE_LAYOUT = 1
        private val LOAD_MORE_LAYOUT = 2
    }

    var endless: Boolean = false
        set(enable) {
            field = enable
            notifyDataSetChanged()
        }

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(RELEASE_LAYOUT, ReleaseItemDelegate(listener))
            addDelegate(LOAD_MORE_LAYOUT, LoadMoreDelegate(listener))
        }
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (endless && count > 0) {
            count++
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (endless && position == itemCount - 1) {
            LOAD_MORE_LAYOUT
        } else super.getItemViewType(position)
    }

    fun insertMore(newItems: List<ReleaseItem>) {
        val prevItems = itemCount
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        notifyItemRangeInserted(prevItems, itemCount)
    }

    fun bindItems(newItems: List<ReleaseItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener, ReleaseItemDelegate.Listener
}
