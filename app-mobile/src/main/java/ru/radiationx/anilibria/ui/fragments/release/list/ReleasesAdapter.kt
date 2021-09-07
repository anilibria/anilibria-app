package ru.radiationx.anilibria.ui.fragments.release.list

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.release.ReleaseItem

/* Created by radiationx on 31.10.17. */

open class ReleasesAdapter(
    var listener: ItemListener,
    private val placeHolder: PlaceholderListItem
) : ListItemAdapter() {

    var endless: Boolean = false
        set(enable) {
            field = enable
            removeLoadMore()
            addLoadMore()
            notifyDiffItems()
        }

    protected val localItems = mutableListOf<ListItem>()

    init {
        addDelegate(ReleaseItemDelegate(listener))
        addDelegate(LoadMoreDelegate(listener))
        addDelegate(PlaceholderDelegate())
    }

    protected fun updatePlaceholder(condition: Boolean = localItems.isEmpty()) {
        if (condition) {
            localItems.add(placeHolder)
        } else {
            localItems.removeAll { it is PlaceholderListItem }
        }
    }

    private fun removeLoadMore() {
        localItems.removeAll { it is LoadMoreListItem }
    }

    protected fun addLoadMore() {
        if (endless) {
            localItems.add(LoadMoreListItem("bottom"))
        }
    }

    fun insertMore(newItems: List<ReleaseItem>) {
        removeLoadMore()
        localItems.addAll(newItems.map { ReleaseListItem(it) })
        addLoadMore()
        notifyDiffItems()
    }

    open fun bindItems(newItems: List<ReleaseItem>) {
        localItems.clear()
        localItems.addAll(newItems.map { ReleaseListItem(it) })
        updatePlaceholder()
        addLoadMore()
        notifyDiffItems()
    }

    fun removeItems(remItems: List<ReleaseItem>) {
        remItems.forEach { remItem ->
            val index = localItems.indexOfFirst {
                it is ReleaseListItem && it.item.id == remItem.id
            }
            if (index != -1) {
                localItems.removeAt(index)
            }
        }
        notifyDiffItems()
    }

    fun updateItems(updItems: List<ReleaseItem>) {
        updItems
            .map { updItem ->
                localItems.indexOfFirst { it is ReleaseListItem && it.item.id == updItem.id } to updItem
            }
            .forEach {
                localItems[it.first] = ReleaseListItem(it.second)
            }
        notifyDiffItems()
    }

    protected fun notifyDiffItems() {
        items = localItems.toList()
    }

    interface ItemListener : LoadMoreDelegate.Listener, ReleaseItemDelegate.Listener
}
