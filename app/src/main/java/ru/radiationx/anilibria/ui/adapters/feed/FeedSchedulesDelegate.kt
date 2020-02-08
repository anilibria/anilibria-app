package ru.radiationx.anilibria.ui.adapters.feed

import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_schedules.*
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.extension.inflate
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.IBundledViewHolder
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.feed.FeedSchedulesAdapter

/**
 * Created by radiationx on 13.01.18.
 */
class FeedSchedulesDelegate(
        private val clickListener: (ScheduleItem, View) -> Unit
) : AppAdapterDelegate<FeedSchedulesListItem, ListItem, FeedSchedulesDelegate.ViewHolder>(
        R.layout.item_feed_schedules,
        { it is FeedSchedulesListItem },
        null
) {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun bindData(item: FeedSchedulesListItem, holder: ViewHolder) =
            holder.bind(item.items)

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(
                parent.inflate(layoutRes!!, false),
                clickListener,
                viewPool
        )
    }

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (ScheduleItem, View) -> Unit,
            private val viewPool: RecyclerView.RecycledViewPool? = null
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer, IBundledViewHolder {

        private val currentItems = mutableListOf<ScheduleItem>()
        private val scheduleAdapter = FeedSchedulesAdapter(clickListener)

        init {
            itemFeedScheduleList.apply {
                isSaveEnabled = false
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = scheduleAdapter
                viewPool?.also {
                    setRecycledViewPool(it)
                }
            }
        }

        fun bind(items: List<ScheduleItem>) {
            currentItems.clear()
            currentItems.addAll(items)
            scheduleAdapter.bindItems(currentItems)
        }

        override fun getStateId(): Int {
            return currentItems.hashCode()
        }

        override fun saveState(): Parcelable? {
            return itemFeedScheduleList.layoutManager?.onSaveInstanceState()
        }

        override fun restoreState(state: Parcelable?) {
            state?.also { itemFeedScheduleList.layoutManager?.onRestoreInstanceState(it) }
        }
    }
}