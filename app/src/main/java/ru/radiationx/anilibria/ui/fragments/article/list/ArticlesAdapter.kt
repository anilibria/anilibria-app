package ru.radiationx.anilibria.ui.fragments.article.list

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.articles.ArticleItemDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import java.util.*

/* Created by radiationx on 31.10.17. */

open class ArticlesAdapter(var listener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    var endless: Boolean = false
        set(enable) {
            field = enable
            removeLoadMore()
            addLoadMore()
            notifyDataSetChanged()
        }

    private val vitalItems = mutableListOf<VitalItem>()

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ArticleItemDelegate(listener))
            addDelegate(LoadMoreDelegate(listener))
            addDelegate(VitalWebItemDelegate())
            addDelegate(VitalNativeItemDelegate())
        }
    }

    private val random = Random()

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }

    fun setVitals(vitals: List<VitalItem>) {
        vitalItems.clear()
        vitalItems.addAll(vitals)
        randomInsertVitals()
    }

    private fun removeLoadMore() {
        this.items.removeAll { it is LoadMoreListItem }
    }

    private fun addLoadMore() {
        if (endless) {
            this.items.add(LoadMoreListItem())
        }
    }

    private fun randomInsertVitals() {
        if (vitalItems.isNotEmpty() && items.isNotEmpty()) {
            val randomIndex = rand(0, Math.min(8, items.size))
            if (randomIndex < 6) {
                val randomVital = if (vitalItems.size > 1) rand(0, vitalItems.size) else 0
                val listItem = getVitalListItem(vitalItems[randomVital])
                this.items.add(items.lastIndex - randomIndex, listItem)
            }
        }
    }

    private fun getVitalListItem(item: VitalItem) = when (item.contentType) {
        VitalItem.ContentType.WEB -> VitalWebListItem(item)
        else -> VitalNativeListItem(item)
    }

    fun insertMore(newItems: List<ArticleItem>) {
        val prevItems = itemCount
        removeLoadMore()
        this.items.addAll(newItems.map { ArticleListItem(it) })
        randomInsertVitals()
        addLoadMore()
        notifyItemRangeInserted(prevItems, itemCount)
    }

    fun bindItems(newItems: List<ArticleItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { ArticleListItem(it) })
        randomInsertVitals()
        addLoadMore()
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener, ArticleItemDelegate.Listener

}
