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
    private val appUpdateListener: () -> Unit,
    private val appUpdateCloseListener: () -> Unit,
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
        addDelegate(AppUpdateCardDelegate(appUpdateListener, appUpdateCloseListener))
        addDelegate(LoadMoreDelegate(null))
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
        super.onBindViewHolder(holder, position, payloads)

        val threshold = (items.lastIndex - position)
        if (threshold <= 3) {
            Handler().post {
                loadMoreListener.invoke()
            }
        }
    }

    fun bindState(state: FeedScreenState) {
        val loadingState = state.data
        val newItems = mutableListOf<ListItem>()

        if (state.hasAppUpdate && (loadingState.data != null || loadingState.error != null)) {
            newItems.add(AppUpdateCardListItem("top"))
        }

        loadingState.data?.schedule?.also { scheduleState ->
            newItems.add(
                FeedSectionListItem(
                    TAG_SCHEDULE_SECTION,
                    scheduleState.title,
                    "Расписание"
                )
            )
            newItems.add(FeedSchedulesListItem("actual", scheduleState.items))
        }

        val feedItems = loadingState.data?.feedItems.orEmpty()
        if (feedItems.isNotEmpty()) {
            newItems.add(FeedSectionListItem(TAG_FEED_SECTION, "Обновления", null, hasBg = true))
            newItems.add(FeedRandomBtnListItem("random"))

            var lastFeedItem: FeedItemState? = null
            feedItems.forEach { feedItem ->
                val isNotReleaseSequence = feedItem.release == null && lastFeedItem?.release != null
                val isNotYoutubeSequence = feedItem.youtube == null && lastFeedItem?.youtube != null
                if (isNotReleaseSequence || isNotYoutubeSequence) {
                    newItems.add(DividerShadowListItem("${feedItem.release?.id}_${feedItem.youtube?.id}"))
                }
                newItems.add(FeedListItem(feedItem))
                lastFeedItem = feedItem
            }
        }

        if (loadingState.hasMorePages) {
            if (loadingState.error != null) {
                newItems.add(LoadErrorListItem("bottom"))
            } else if (loadingState.moreLoading) {
                newItems.add(LoadMoreListItem("bottom"))
            }
        }

        items = newItems
    }
}
