package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.FeedReleaseDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedYoutubeDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import kotlin.reflect.KClass

/* Created by radiationx on 31.10.17. */

class FeedAdapter(
        private val listener: ItemListener,
        private val scheduleListener: (ReleaseItem) -> Unit,
        private val placeHolder: PlaceholderListItem
) : OptimizeAdapter<MutableList<ListItem>>() {


    private val scheduleSection = FeedSectionListItem("Ожидаются сегодня")
    private val feedSection = FeedSectionListItem("Обновления")

    private val sectionsSequence = listOf(scheduleSection, feedSection)
    private val sectionItemTypes: Map<ListItem, List<KClass<out ListItem>>> = mapOf(
            scheduleSection to emptyList(),
            feedSection to listOf(
                    FeedListItem::class
            )
    )

    init {
        items = mutableListOf()
        addDelegate(LoadMoreDelegate(object : LoadMoreDelegate.Listener {
            override fun onLoadMore() {}
        }))
        addDelegate(FeedSectionDelegate())
        addDelegate(FeedSchedulesDelegate(scheduleListener))
        addDelegate(FeedReleaseDelegate(listener))
        addDelegate(FeedYoutubeDelegate(object : FeedYoutubeDelegate.Listener {
            override fun onItemClick(item: YoutubeItem, position: Int) {

            }

            override fun onItemLongClick(item: YoutubeItem): Boolean {
                return false
            }
        }))
        items.add(FeedSectionListItem("Обновления"))
    }


    fun bindSchedules(newItems: List<ReleaseItem>) {
        val index = items.indexOfFirst {
            (it as? FeedSectionListItem)?.id == SECTION_SCHEDULE
        }

        if (newItems.isEmpty()) {
            if (index != -1) {
                items.removeAt(index)
                items.removeAt(index)
                notifyItemRangeRemoved(index, 2)
            }
        } else {
            if (index == -1) {
                items.addAll(0, listOf(
                        FeedSectionListItem(SECTION_SCHEDULE, "Ожидаются сегодня"),
                        FeedSchedulesListItem(newItems)
                ))
                notifyItemRangeInserted(0, 2)
            } else {
                items[index + 1] = FeedSchedulesListItem(newItems)
                notifyItemChanged(index + 1)
            }
        }
    }

    fun bindItems(newItems: List<FeedItem>) {

        val progress = isProgress()

        var startIndex = items.indexOfLast {
            (it is FeedSectionListItem)
        } + 1

        val currentFeedItems = items.filterIsInstance<FeedListItem>()
        val newListItems = newItems.map { FeedListItem(it) }

        currentFeedItems.forEach { items.remove(it) }
        items.addAll(startIndex, newListItems)


        when {
            currentFeedItems.size == newListItems.size -> {
                notifyItemRangeChanged(startIndex, currentFeedItems.size)
            }
            currentFeedItems.size < newListItems.size -> {
                val insertIndex = startIndex + currentFeedItems.size
                notifyItemRangeChanged(startIndex, currentFeedItems.size)
                notifyItemRangeInserted(insertIndex, newListItems.size - currentFeedItems.size)
            }
            currentFeedItems.size > newListItems.size -> {
                val removeIndex = startIndex + newListItems.size
                notifyItemRangeChanged(startIndex, newListItems.size)
                notifyItemRangeRemoved(removeIndex, currentFeedItems.size - newListItems.size)
            }
        }


        val loadMoreIndex = items.indexOfLast { it is LoadMoreListItem }
        if (loadMoreIndex != -1) {
            items.removeAt(loadMoreIndex)
            notifyItemRemoved(loadMoreIndex)
        }

        if (progress) {
            items.add(LoadMoreListItem)
            notifyItemInserted(items.size)
        }
    }

    fun showProgress(isVisible: Boolean) {
        val progress = isProgress()

        if (isVisible && !progress) {
            items.add(LoadMoreListItem)
            notifyItemInserted(items.lastIndex)
        } else if (!isVisible && progress) {
            items.remove(items.last())
            notifyItemRemoved(items.size)
        }
    }

    private fun isProgress() = items.isNotEmpty() && items.last() is LoadMoreListItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val time = System.currentTimeMillis()
        return super.onCreateViewHolder(parent, viewType).also {
            Log.d("nonono", "onCreateViewHolder  type=${viewType} time=${System.currentTimeMillis() - time}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any?>) {

        val time = System.currentTimeMillis()
        super.onBindViewHolder(holder, position, payloads)

        Log.d("nonono", "onBindViewHolder pos=$position type=${getItemViewType(position)} time=${System.currentTimeMillis() - time}")
        val threshold = (items.lastIndex - position)
        if (threshold <= 3) {
            Handler().post {
                listener.onLoadMore()
            }
        }
    }



    // start magic part

    private fun getNearestSectionAnchor(anchor: ListItem): ListItem? {
        val existIndex = items.indexOf(anchor)
        if (existIndex == -1) {
            val seqIndex = sectionsSequence.indexOf(anchor)

            var targetSection: ListItem? = null
            for (i in seqIndex downTo 0) {
                val section = sectionsSequence[i]
                if (items.indexOf(section) != -1) {
                    targetSection = section
                    break
                }
            }
            if (targetSection == null) {
                for (i in seqIndex until sectionsSequence.size) {
                    val section = sectionsSequence[i]
                    if (items.indexOf(section) != -1) {
                        targetSection = section
                        break
                    }
                }
            }

            targetSection?.also { section ->
                val sectionTypes = sectionItemTypes.getValue(section)
                val sectionIndex = items.indexOf(section)
                var lastAnchor: ListItem = section
                for (i in sectionIndex until items.size) {
                    val item = items[i]
                    if (sectionTypes.any { it == item::class }) {
                        lastAnchor = item
                    }
                }
                return lastAnchor
            }
        } else {
            return anchor
        }
        return null
    }

    private fun updateSectionHeader(header: ListItem, isVisible: Boolean) {
        if (isVisible) {
            removeAndNotify(listOf(header))
            val anchor = getNearestSectionAnchor(header)
            insertAndNotify(anchor, listOf(header))
        } else {
            removeAndNotify(listOf(header))
        }
    }

    private fun updateSectionItems(header: ListItem, sectionItems: List<ListItem>, isVisible: Boolean) {
        if (isVisible) {
            removeAndNotify(sectionItems)
            val anchor = getNearestSectionAnchor(header)
            insertAndNotify(anchor, sectionItems)
        } else {
            removeAndNotify(sectionItems)
        }
    }

    private fun notifyListItemUpdate(item: ListItem) {
        val index = items.indexOf(item)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    private fun removeAndNotify(itemsToRemove: List<ListItem>) {
        itemsToRemove.forEach {
            val index = items.indexOf(it)
            if (index != -1) {
                items.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    private fun insertAndNotify(anchor: ListItem?, newItems: List<ListItem>) {
        val startIndex = items.indexOf(anchor) + 1
        items.addAll(startIndex, newItems)
        notifyItemRangeInserted(startIndex, newItems.size)
    }
    // end magic part


    interface ItemListener : LoadMoreDelegate.Listener, FeedReleaseDelegate.Listener
}
