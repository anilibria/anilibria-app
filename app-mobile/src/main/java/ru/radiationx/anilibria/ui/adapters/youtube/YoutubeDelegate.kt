package ru.radiationx.anilibria.ui.adapters.youtube

import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedYoutubeBinding
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class YoutubeDelegate(
    private val itemListener: Listener
) : AppAdapterDelegate<YoutubeListItem, ListItem, YoutubeDelegate.ViewHolder>(
    R.layout.item_feed_youtube,
    { it is YoutubeListItem },
    { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: YoutubeListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedYoutubeBinding>()

        fun bind(item: YoutubeListItem) {
            binding.run {
                itemTitle.text = item.state.title

                itemViewsCount.text = item.state.views
                itemCommentsCount.text = item.state.comments

                itemImage.showImageUrl(item.state.image)
                ViewCompat.setTransitionName(
                    itemImage,
                    "${item.javaClass.simpleName}_${item.state.id}"
                )
                root.setOnClickListener {
                    itemListener.onItemClick(item.state, layoutPosition)
                }
            }
        }
    }

    interface Listener : BaseItemListener<YoutubeItemState>
}