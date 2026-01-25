package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedRandomBtnBinding
import ru.radiationx.anilibria.ui.adapters.FeedRandomBtnListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 13.01.18.
 */
class FeedRandomBtnDelegate(
    private val clickListener: () -> Unit
) : AppAdapterDelegate<FeedRandomBtnListItem, ListItem, FeedRandomBtnDelegate.ViewHolder>(
    R.layout.item_feed_random_btn,
    { it is FeedRandomBtnListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: FeedRandomBtnListItem, holder: ViewHolder) =
        holder.bind()

    class ViewHolder(
        itemView: View,
        private val clickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedRandomBtnBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            binding.itemRandomBtn.setOnClickListener {
                clickListener.invoke()
            }
        }

        fun bind() {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
        }
    }
}