package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.FeedReleaseDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedYoutubeDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.adapters.youtube.YoutubeDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter

/* Created by radiationx on 31.10.17. */

open class FeedAdapter(
        private val listener: ItemListener,
        private val scheduleListener: (ReleaseItem) -> Unit,
        private val placeHolder: PlaceholderListItem
) : OptimizeAdapter<MutableList<ListItem>>() {


    init {
        items = mutableListOf()
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
        items.add(FeedSectionListItem("Ожидаются сегодня"))
        items.add(FeedSchedulesListItem(emptyList()))
        items.add(FeedSectionListItem("Обновления"))
    }

    fun bindSchedules(newItems: List<ReleaseItem>) {
        val index = items.indexOfFirst { it is FeedSchedulesListItem }
        if (index != -1) {
            items[index] = FeedSchedulesListItem(newItems)
            notifyItemChanged(index)
        }
    }

    open fun bindItems(newItems: List<ReleaseItem>) {
        items.removeAll { it is ReleaseListItem }
        val index = items.indexOfLast { it is FeedSectionListItem }
        this.items.addAll(index + 1, newItems.map { ReleaseListItem(it) })
        items.add(index + 2, YoutubeListItem(YoutubeItem().apply {
            title = "О ЗАБЛУЖДЕНИИ ПРО «БЕЛЬЁ» И АТАКЕ БЛОКИРОВЩИКА РЕКЛАМЫ | ЛЛН"
            image = "https://www.anilibria.tv/upload/youtube/da281ce3.jpg"
            views = 15029
            comments = 444
        }))
        notifyDataSetChanged()
    }

    fun insertMore(newItems: List<ReleaseItem>) {
        val prevItems = itemCount
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        notifyItemRangeInserted(prevItems, itemCount)
    }


    interface ItemListener : LoadMoreDelegate.Listener, FeedReleaseDelegate.Listener
}
