package ru.radiationx.anilibria.ui.adapters.feed

import android.support.v7.widget.RecyclerView
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_schedule.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class FeedScheduleDelegate(
        private val itemListener: (ReleaseItem) -> Unit
) : AppAdapterDelegate<FeedScheduleListItem, ListItem, FeedScheduleDelegate.ViewHolder>(
        R.layout.item_feed_schedule,
        { it is FeedScheduleListItem },
        { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: FeedScheduleListItem, holder: ViewHolder) =
            holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            private val itemListener: (ReleaseItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: ReleaseItem

        init {
            containerView.setOnClickListener {
                itemListener.invoke(currentItem)
            }
        }

        fun bind(item: ReleaseItem) {
            currentItem = item
            ImageLoader.getInstance().displayImage(item.poster, item_image)
        }
    }
}