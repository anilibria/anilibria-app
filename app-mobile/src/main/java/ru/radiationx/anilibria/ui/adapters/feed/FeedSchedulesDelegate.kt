package ru.radiationx.anilibria.ui.adapters.feed

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedSchedulesBinding
import ru.radiationx.anilibria.extension.addItemsPositionListener
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.IBundledViewHolder
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.feed.FeedSchedulesAdapter
import ru.radiationx.shared.ktx.android.inflate

/**
 * Created by radiationx on 13.01.18.
 */
class FeedSchedulesDelegate(
    private val clickListener: (ScheduleItemState, View, Int) -> Unit,
    private val scrollListener: (Int) -> Unit,
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
            scrollListener,
            viewPool
        )
    }

    class ViewHolder(
        itemView: View,
        clickListener: (ScheduleItemState, View, Int) -> Unit,
        private val scrollListener: (Int) -> Unit,
        private val viewPool: RecyclerView.RecycledViewPool? = null,
    ) : RecyclerView.ViewHolder(itemView), IBundledViewHolder {

        private val binding by viewBinding<ItemFeedSchedulesBinding>()

        private val currentItems = mutableListOf<ScheduleItemState>()
        private val scheduleAdapter = FeedSchedulesAdapter(clickListener)

        init {
            binding.itemFeedScheduleList.apply {
                isSaveEnabled = false
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = scheduleAdapter
                disableItemChangeAnimation()
                viewPool?.also {
                    setRecycledViewPool(it)
                }
                var prevScrollPosition = -1
                addItemsPositionListener { _, last ->
                    if (prevScrollPosition != last) {
                        scrollListener.invoke(last)
                        prevScrollPosition = last
                    }
                }
            }
        }

        fun bind(items: List<ScheduleItemState>) {
            currentItems.clear()
            currentItems.addAll(items)
            scheduleAdapter.bindItems(currentItems)
            binding.itemFeedScheduleList.isVisible = currentItems.isNotEmpty()
            binding.itemFeedScheduleEmpty.isVisible = currentItems.isEmpty()
        }

        override fun getStateId(): Int {
            return currentItems.hashCode()
        }

        override fun saveState(): Parcelable? {
            return binding.itemFeedScheduleList.layoutManager?.onSaveInstanceState()
        }

        override fun restoreState(state: Parcelable?) {
            state?.also { binding.itemFeedScheduleList.layoutManager?.onRestoreInstanceState(it) }
        }
    }
}