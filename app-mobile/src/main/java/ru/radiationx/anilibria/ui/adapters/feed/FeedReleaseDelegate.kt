package ru.radiationx.anilibria.ui.adapters.feed

import android.text.Html
import android.view.View
import androidx.core.text.parseAsHtml
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedReleaseBinding
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.FeedListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class FeedReleaseDelegate(
    private val clickListener: (ReleaseItemState, View) -> Unit,
    private val longClickListener: (ReleaseItemState, View) -> Unit
) : AppAdapterDelegate<FeedListItem, ListItem, FeedReleaseDelegate.ViewHolder>(
    R.layout.item_feed_release,
    { (it as? FeedListItem)?.item?.release != null },
    { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 5

    override fun bindData(item: FeedListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ReleaseItemState, View) -> Unit,
        private val longClickListener: (ReleaseItemState, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedReleaseBinding>()

        fun bind(item: FeedListItem) {
            val state = requireNotNull(item.item.release)

            binding.itemTitle.text = state.title
            binding.itemDesc.text = state.description
            ViewCompat.setTransitionName(
                binding.itemImage,
                "${item.javaClass.simpleName}_${state.id}"
            )
            binding.itemNewIndicator.isVisible = state.isNew
            binding.itemImage.showImageUrl(state.posterUrl)


            binding.root.setOnClickListener {
                clickListener.invoke(state, binding.itemImage)
            }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(state, binding.itemImage)
                return@setOnLongClickListener false
            }
        }
    }
}