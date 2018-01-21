package ru.radiationx.anilibria.ui.adapters.global

/* Created by radiationx on 18.11.17. */

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.ui.adapters.CommentListItem
import ru.radiationx.anilibria.ui.adapters.ListItem

class CommentsAdapter(var listener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {
    companion object {
        private val COMMENT_LAYOUT = 1
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
            addDelegate(COMMENT_LAYOUT, CommentDelegate())
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


    fun setComments(comments: List<Comment>) {
        items.clear()
        items.addAll(comments.map { CommentListItem(it) })
        notifyDataSetChanged()
    }

    fun addComments(comments: List<Comment>) {
        items.addAll(comments.map { CommentListItem(it) })
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener
}
