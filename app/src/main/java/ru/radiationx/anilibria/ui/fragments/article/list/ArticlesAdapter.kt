package ru.radiationx.anilibria.ui.fragments.article.list

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.ui.adapters.ArticleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.articles.ArticleItemDelegate

/* Created by radiationx on 31.10.17. */

open class ArticlesAdapter(var listener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    companion object {
        private val ARTICLE_LAYOUT = 1
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
            addDelegate(ARTICLE_LAYOUT, ArticleItemDelegate(listener))
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


    fun insertMore(newItems: List<ArticleItem>) {
        val prevItems = itemCount
        this.items.addAll(newItems.map { ArticleListItem(it) })
        notifyItemRangeInserted(prevItems, itemCount)
    }

    fun bindItems(newItems: List<ArticleItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { ArticleListItem(it) })
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener, ArticleItemDelegate.Listener

}
