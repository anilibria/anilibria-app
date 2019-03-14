package ru.radiationx.anilibria.ui.adapters.feed

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_section_header.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class FeedSectionDelegate(
        private val clickListener: (FeedSectionListItem) -> Unit
) : AppAdapterDelegate<FeedSectionListItem, ListItem, FeedSectionDelegate.ViewHolder>(
        R.layout.item_feed_section_header,
        { it is FeedSectionListItem },
        { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 2

    override fun bindData(item: FeedSectionListItem, holder: ViewHolder) =
            holder.bind(item)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (FeedSectionListItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: FeedSectionListItem

        init {
            containerView.setOnClickListener {
                clickListener.invoke(currentItem)
            }
            itemFeedScheduleBtn.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    itemFeedScheduleBtn.getCompatDrawable(R.drawable.ic_chevron_right),
                    null
            )
        }

        fun bind(item: FeedSectionListItem) {
            currentItem = item
            itemFeedScheduleTitle.text = item.title
            itemFeedScheduleBtn.visible(item.route != null)
            itemFeedScheduleBtn.text = item.route
        }
    }
}