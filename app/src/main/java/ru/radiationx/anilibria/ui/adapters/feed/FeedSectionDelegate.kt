package ru.radiationx.anilibria.ui.adapters.feed

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_section_header.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class FeedSectionDelegate : AppAdapterDelegate<FeedSectionListItem, ListItem, FeedSectionDelegate.ViewHolder>(
        R.layout.item_feed_section_header,
        { it is FeedSectionListItem },
        { ViewHolder(it) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 2

    override fun bindData(item: FeedSectionListItem, holder: ViewHolder) =
            holder.bind(item.title)

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
        }

        fun bind(title: String) {
            itemFeedScheduleTitle.text = title
        }
    }
}