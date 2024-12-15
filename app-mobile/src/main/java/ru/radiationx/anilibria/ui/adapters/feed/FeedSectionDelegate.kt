package ru.radiationx.anilibria.ui.adapters.feed

import android.graphics.Color
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedSectionHeaderBinding
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.shared.ktx.android.setCompatDrawable

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
        itemView: View,
        private val clickListener: (FeedSectionListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedSectionHeaderBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            binding.itemFeedScheduleBtn.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                binding.itemFeedScheduleBtn.getCompatDrawable(R.drawable.ic_chevron_right),
                null
            )
        }

        fun bind(item: FeedSectionListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemFeedScheduleTitle.text = item.title
            binding.itemFeedScheduleTitle.gravity = if (item.center) {
                Gravity.CENTER
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
            binding.itemFeedScheduleBtn.isVisible = item.route != null
            binding.itemFeedScheduleBtn.text = item.route
            binding.itemFeedScheduleIcon.isVisible = item.routeIconRes != null
            item.routeIconRes?.also {
                binding.itemFeedScheduleIcon.setCompatDrawable(it)
            }

            binding.root.setBackgroundColor(
                if (item.hasBg) {
                    binding.root.context.getColorFromAttr(R.attr.colorSurface)
                } else {
                    Color.TRANSPARENT
                }
            )
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}