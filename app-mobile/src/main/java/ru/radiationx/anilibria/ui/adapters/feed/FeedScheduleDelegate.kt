package ru.radiationx.anilibria.ui.adapters.feed

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedScheduleBinding
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.FeedScheduleListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.shared.ktx.android.dpToPx
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class FeedScheduleDelegate(
    private val clickListener: (ScheduleItemState, View, Int) -> Unit
) : AppAdapterDelegate<FeedScheduleListItem, ListItem, FeedScheduleDelegate.ViewHolder>(
    R.layout.item_feed_schedule,
    { it is FeedScheduleListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: FeedScheduleListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ScheduleItemState, View, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedScheduleBinding>()

        init {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#99ffffff"),
                    Color.parseColor("#99ffffff"),
                    Color.parseColor("#00ffffff")
                )
            ).also {
                it.cornerRadius = 0f
                it.gradientRadius = binding.itemComplete.dpToPx(20).toFloat()
                it.gradientType = GradientDrawable.RADIAL_GRADIENT
            }
            binding.itemComplete.background = gradientDrawable
        }

        @Suppress("DEPRECATION")
        fun bind(item: FeedScheduleListItem) {
            val state = item.state
            binding.itemComplete.isVisible = state.isCompleted
            ViewCompat.setTransitionName(
                binding.itemImage,
                "${item.javaClass.simpleName}_${state.releaseId}"
            )
            binding.itemImage.showImageUrl(state.posterUrl)

            binding.root.setOnClickListener {
                clickListener.invoke(state, binding.itemImage, adapterPosition)
            }
        }
    }
}