package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

/* Created by radiationx on 31.10.17. */

class FeedAdapter(
    val loadMoreListener: () -> Unit,
    schedulesClickListener: () -> Unit,
    scheduleScrollListener: (Int) -> Unit,
    randomClickListener: () -> Unit,
    releaseClickListener: (ReleaseItem, View) -> Unit,
    releaseLongClickListener: (ReleaseItem, View) -> Unit,
    youtubeClickListener: (YoutubeItem, View) -> Unit,
    scheduleClickListener: (ScheduleItem, View, Int) -> Unit
) : ListItemAdapter() {

    private val scheduleSection = FeedSectionListItem("Ожидаются", "Расписание")
    private val feedSection = FeedSectionListItem("Обновления", hasBg = true)

    private val sectionClickListener = { item: FeedSectionListItem ->
        if (item == scheduleSection) {
            schedulesClickListener.invoke()
        }
    }

    private val localItems = mutableListOf<ListItem>()

    init {
        addDelegate(LoadMoreDelegate(object : LoadMoreDelegate.Listener {
            override fun onLoadMore() {}
        }))
        addDelegate(FeedSectionDelegate(sectionClickListener))
        addDelegate(FeedSchedulesDelegate(scheduleClickListener, scheduleScrollListener))
        addDelegate(FeedReleaseDelegate(releaseClickListener, releaseLongClickListener))
        addDelegate(FeedYoutubeDelegate(youtubeClickListener))
        addDelegate(FeedRandomBtnDelegate(randomClickListener))
        addDelegate(DividerShadowItemDelegate())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any?>
    ) {

        val time = System.currentTimeMillis()
        super.onBindViewHolder(holder, position, payloads)

        val threshold = (localItems.lastIndex - position)
        if (threshold <= 3) {
            Handler().post {
                loadMoreListener.invoke()
            }
        }
    }

    fun bindSchedules(title: String, newItems: List<ScheduleItem>) {
        val index = localItems.indexOf(scheduleSection)
        scheduleSection.title = title

        if (index == -1) {
            localItems.addAll(
                0, listOf(
                    scheduleSection,
                    FeedSchedulesListItem("actual", newItems)
                )
            )
        } else {
            localItems[index + 1] = FeedSchedulesListItem("actual", newItems)
        }


        notifyDiffItems()
    }

    fun updateItems(updItems: List<FeedItem>) {
        val updListItems = localItems
            .filterIsInstance<FeedListItem>()
            .filter { feedListItem ->
                val releaseItem = feedListItem.item.release
                val youtubeItem = feedListItem.item.youtube
                when {
                    releaseItem != null -> updItems.firstOrNull { it.release?.id == releaseItem.id } != null
                    youtubeItem != null -> updItems.firstOrNull { it.youtube?.id == youtubeItem.id } != null
                    else -> false
                }
            }

        updListItems.forEach { feedListItem ->
            val index = localItems.indexOf(feedListItem)
            val updItem = updItems.firstOrNull { feedItem ->
                val releaseItem = feedListItem.item.release
                val youtubeItem = feedListItem.item.youtube
                when {
                    releaseItem != null -> feedItem.release?.id == releaseItem.id
                    youtubeItem != null -> feedItem.youtube?.id == youtubeItem.id
                    else -> false
                }
            }
            if (index != -1 && updItem != null) {
                localItems[index] = FeedListItem(updItem)
                notifyDiffItems()
            }
        }
    }

    fun bindItems(newItems: List<FeedItem>) {

        val progress = isProgress()

        var startIndex = localItems.indexOf(feedSection)


        if (startIndex == -1) {
            startIndex = localItems.indexOfLast { it is FeedSchedulesListItem }
            //localItems.add(DividerShadowListItem())
            localItems.add(feedSection)
            localItems.add(FeedRandomBtnListItem("random"))
        }
        startIndex = localItems.indexOf(feedSection) + 2


        val currentFeedItems = (startIndex until itemCount)
            .filter { index ->
                val listItem = localItems[index]
                listItem is FeedListItem || listItem is DividerShadowListItem
            }
            .map { localItems[it] }
        val newListItems = mutableListOf<ListItem>()

        var lastFeedItem: FeedListItem? = null
        newItems.forEach { feedItem ->
            lastFeedItem?.also {
                if (it.item.release == null && feedItem.release != null
                    || it.item.youtube == null && feedItem.youtube != null
                ) {
                    newListItems.add(DividerShadowListItem("item_${it.item.hashCode()}_${feedItem.hashCode()}"))
                }
            }
            newListItems.add(FeedListItem(feedItem).also {
                lastFeedItem = it
            })
        }

        currentFeedItems.forEach { localItems.remove(it) }
        localItems.addAll(startIndex, newListItems)


        val loadMoreIndex = localItems.indexOfLast { it is LoadMoreListItem }
        if (loadMoreIndex != -1) {
            localItems.removeAt(loadMoreIndex)
        }

        if (progress) {
            localItems.add(LoadMoreListItem("bottom"))
        }
        notifyDiffItems()
    }

    fun showProgress(isVisible: Boolean) {
        val progress = isProgress()

        if (isVisible && !progress) {
            localItems.add(LoadMoreListItem("bottom"))
        } else if (!isVisible && progress) {
            localItems.remove(localItems.last())
        }
        notifyDiffItems()
    }

    private fun notifyDiffItems() {
        items = localItems.toList()
    }

    private fun isProgress() = localItems.isNotEmpty() && localItems.last() is LoadMoreListItem

}
