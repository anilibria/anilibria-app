package ru.radiationx.anilibria.ui.adapters.global

/* Created by radiationx on 18.11.17. */

import android.util.Log
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.ui.adapters.*

class CommentsAdapter(
        var listener: ItemListener,
        private val placeHolder: PlaceholderListItem
) : ListDelegationAdapter<MutableList<ListItem>>() {
    companion object {
        /*private const val COMMENT_LAYOUT = 1
        private const val LOAD_MORE_LAYOUT = 2
        private const val PLACEHOLDER_LAYOUT = 3*/
    }

    var endless: Boolean = false
        set(enable) {
            field = enable
            removeLoadMore()
            addLoadMore()
            notifyDataSetChanged()
        }

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(CommentDelegate(listener))
            addDelegate(LoadMoreDelegate(listener))
            addDelegate(PlaceholderDelegate())
        }
    }

    /*override fun getItemCount(): Int {
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
    }*/

    private fun updatePlaceholder(condition: Boolean = items.isEmpty()) {
        Log.e("lululu", "updatePlaceholder $condition, ${items.indexOfFirst { it is PlaceholderListItem } != -1}, ${items.joinToString { it.javaClass.canonicalName }}")
        if (items.indexOfFirst { it is PlaceholderListItem } != -1) {
            return
        }
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


    fun setComments(newItems: List<Comment>) {
        items.clear()
        items.addAll(newItems.map { CommentListItem(it) })
        updatePlaceholder()
        addLoadMore()
        notifyDataSetChanged()
    }

    fun addComments(newItems: List<Comment>) {
        val prevItems = itemCount
        removeLoadMore()
        items.addAll(newItems.map { CommentListItem(it) })
        addLoadMore()
        notifyItemRangeInserted(prevItems, itemCount)
    }

    interface ItemListener : LoadMoreDelegate.Listener, CommentDelegate.Listener
}
