package ru.radiationx.anilibria.ui.adapters.release.list

import android.view.View
import androidx.core.text.parseAsHtml
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFeedReleaseBinding
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseItemDelegate(
    private val clickListener: (ReleaseItemState, View) -> Unit,
    private val longClickListener: (ReleaseItemState) -> Unit,
) : AppAdapterDelegate<ReleaseListItem, ListItem, ReleaseItemDelegate.ViewHolder>(
    R.layout.item_feed_release,
    { it is ReleaseListItem },
    { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: ReleaseListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ReleaseItemState, View) -> Unit,
        private val longClickListener: (ReleaseItemState) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFeedReleaseBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: ReleaseListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            val releaseItem = item.item
            binding.itemTitle.text = releaseItem.title

            binding.itemDesc.text = releaseItem.description.parseAsHtml()
            ViewCompat.setTransitionName(
                binding.itemImage,
                "${item.javaClass.simpleName}_${releaseItem.id}"
            )
            binding.itemNewIndicator.isVisible = releaseItem.isNew
            binding.itemImage.showImageUrl(releaseItem.posterUrl)

            binding.root.setOnClickListener {
                clickListener.invoke(releaseItem, binding.itemImage)
            }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(releaseItem)
                true
            }
        }
    }
}