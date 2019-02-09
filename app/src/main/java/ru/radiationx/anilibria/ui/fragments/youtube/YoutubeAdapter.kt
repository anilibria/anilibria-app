package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import java.util.*

/* Created by radiationx on 31.10.17. */

open class YoutubeAdapter(
        var listener: ItemListener,
        private val placeHolder: PlaceholderListItem
) : OptimizeAdapter<MutableList<ListItem>>() {

    private val vitalItems = mutableListOf<VitalItem>()
    private val random = Random()

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
        addDelegate(VitalWebItemDelegate())
        addDelegate(VitalNativeItemDelegate())
    }

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }

    protected fun updatePlaceholder(condition: Boolean = items.isEmpty()) {
        if (condition) {
            items.add(placeHolder)
        } else {
            items.removeAll { it is PlaceholderListItem }
        }
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

    fun insertMore(newItems: List<YoutubeItem>) {
        val prevItems = itemCount
        removeLoadMore()
        this.items.addAll(newItems.map { YoutubeListItem(it) })
        randomInsertVitals()
        addLoadMore()
        notifyItemRangeInserted(prevItems, itemCount)
    }

    fun bindItems(newItems: List<YoutubeItem>) {
        this.items.clear()
        this.items.addAll(newItems.map { YoutubeListItem(it) })
        updatePlaceholder()
        randomInsertVitals()
        addLoadMore()
        notifyDataSetChanged()
    }

    interface ItemListener : LoadMoreDelegate.Listener, YoutubeDelegate.Listener

}
