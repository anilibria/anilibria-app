package ru.radiationx.anilibria.ui.adapters.feed

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_schedules.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.feed.FeedSchedulesAdapter

/**
 * Created by radiationx on 13.01.18.
 */
class FeedSchedulesDelegate(
        private val clickListener: (FeedScheduleItem, View) -> Unit
) : AppAdapterDelegate<FeedSchedulesListItem, ListItem, FeedSchedulesDelegate.ViewHolder>(
        R.layout.item_feed_schedules,
        { it is FeedSchedulesListItem },
        { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 1

    override fun bindData(item: FeedSchedulesListItem, holder: ViewHolder) =
            holder.bind(item.items)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (FeedScheduleItem, View) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val currentItems = mutableListOf<FeedScheduleItem>()
        private val scheduleAdapter = FeedSchedulesAdapter(clickListener)

        init {
            itemFeedScheduleList.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = scheduleAdapter
            }
        }

        fun bind(items: List<FeedScheduleItem>) {
            currentItems.clear()
            currentItems.addAll(items)
            scheduleAdapter.bindItems(currentItems)
        }
    }
}