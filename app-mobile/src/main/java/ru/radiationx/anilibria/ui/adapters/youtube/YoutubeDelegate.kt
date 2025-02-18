package ru.radiationx.anilibria.ui.adapters.youtube

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedYoutubeBinding
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class YoutubeDelegate(
    private val clickListener: (YoutubeItemState) -> Unit,
    private val longClickListener: (YoutubeItemState) -> Unit,
) : AppAdapterDelegate<YoutubeListItem, ListItem, YoutubeDelegate.ViewHolder>(
    R.layout.item_feed_youtube,
    { it is YoutubeListItem },
    { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: YoutubeListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (YoutubeItemState) -> Unit,
        private val longClickListener: (YoutubeItemState) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedYoutubeBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: YoutubeListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.run {
                itemTitle.text = item.state.title

                itemViewsCount.text = item.state.views
                itemCommentsCount.text = item.state.comments

                itemImage.showImageUrl(item.state.image)
                root.setOnClickListener {
                    clickListener.invoke(item.state)
                }
                root.setOnLongClickListener {
                    longClickListener.invoke(item.state)
                    true
                }
            }
        }
    }
}