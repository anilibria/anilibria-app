package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.model.FeedItemState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.*
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/* Created by radiationx on 31.10.17. */

class FeedAdapter(
    private val loadMoreListener: () -> Unit,
    private val loadRetryListener: () -> Unit,
    schedulesClickListener: () -> Unit,
    scheduleScrollListener: (Int) -> Unit,
    randomClickListener: () -> Unit,
    releaseClickListener: (ReleaseItemState, View) -> Unit,
    releaseLongClickListener: (ReleaseItemState, View) -> Unit,
    youtubeClickListener: (YoutubeItemState, View) -> Unit,
    scheduleClickListener: (ScheduleItemState, View, Int) -> Unit
) : ListItemAdapter() {

    companion object {
        private const val TAG_SCHEDULE_SECTION = "schedule"
        private const val TAG_FEED_SECTION = "feed"
    }

    private val sectionClickListener = { item: FeedSectionListItem ->
        if (item.tag == TAG_SCHEDULE_SECTION) {
            schedulesClickListener.invoke()
        }
    }

    init {
        addDelegate(LoadMoreDelegate(object : LoadMoreDelegate.Listener {
            override fun onLoadMore() {}
        }))
        addDelegate(LoadErrorDelegate(loadRetryListener))
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

        val threshold = (items.lastIndex - position)
        if (threshold <= 3) {
            Handler().post {
                loadMoreListener.invoke()
            }
        }
    }

    fun bindState(state: FeedScreenState) {
        val newItems = mutableListOf<ListItem>()

        if (state.schedule != null) {
            newItems.add(
                FeedSectionListItem(
                    TAG_SCHEDULE_SECTION,
                    state.schedule.title,
                    "Расписание"
                )
            )
            newItems.add(FeedSchedulesListItem("actual", state.schedule.items))
        }

        if (state.feedItems.isNotEmpty()) {
            newItems.add(FeedSectionListItem(TAG_FEED_SECTION, "Обновления", null, hasBg = true))
            newItems.add(FeedRandomBtnListItem("random"))

            var lastFeedItem: FeedItemState? = null
            state.feedItems.forEach { feedItem ->
                val isNotReleaseSequence = feedItem.release == null && lastFeedItem?.release != null
                val isNotYoutubeSequence = feedItem.youtube == null && lastFeedItem?.youtube != null
                if (isNotReleaseSequence || isNotYoutubeSequence) {
                    newItems.add(DividerShadowListItem("${feedItem.release?.id}_${feedItem.youtube?.id}"))
                }
                newItems.add(FeedListItem(feedItem))
                lastFeedItem = feedItem
            }
        }

        if (state.hasMorePages) {
            if (state.hasError) {
                newItems.add(LoadErrorListItem("bottom"))
            } else {
                newItems.add(LoadMoreListItem("bottom"))
            }
        }

        items = newItems
    }
}
