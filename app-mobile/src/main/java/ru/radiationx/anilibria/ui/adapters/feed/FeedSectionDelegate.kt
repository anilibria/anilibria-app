package ru.radiationx.anilibria.ui.adapters.feed

import android.graphics.Color
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_section_header.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.visible

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
            itemFeedScheduleTitle.gravity = if (item.center) {
                Gravity.CENTER
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
            itemFeedScheduleBtn.visible(item.route != null)
            itemFeedScheduleBtn.text = item.route
            itemFeedScheduleIcon.isVisible = item.routeIconRes != null
            item.routeIconRes?.also {
                itemFeedScheduleIcon.setCompatDrawable(it)
            }

            containerView.setBackgroundColor(
                if (item.hasBg) {
                    containerView.context.getColorFromAttr(R.attr.colorSurface)
                } else {
                    Color.TRANSPARENT
                }
            )
        }
    }
}