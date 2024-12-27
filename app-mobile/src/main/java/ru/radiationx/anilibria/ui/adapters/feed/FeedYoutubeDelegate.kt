package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedYoutubeBinding
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.FeedListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class FeedYoutubeDelegate(
    private val clickListener: (YoutubeItemState) -> Unit,
    private val longClickListener: (YoutubeItemState) -> Unit,
) : AppAdapterDelegate<FeedListItem, ListItem, FeedYoutubeDelegate.ViewHolder>(
    R.layout.item_feed_youtube,
    { (it as? FeedListItem)?.item?.youtube != null },
    { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 5

    override fun bindData(item: FeedListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (YoutubeItemState) -> Unit,
        private val longClickListener: (YoutubeItemState) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedYoutubeBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: FeedListItem) {
            val state = requireNotNull(item.item.youtube)
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.apply {
                itemTitle.text = state.title

                itemViewsCount.text = state.views
                itemCommentsCount.text = state.comments

                itemImage.showImageUrl(state.image)
            }
            binding.root.setOnClickListener {
                clickListener.invoke(state)
            }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(state)
                true
            }
        }
    }
}