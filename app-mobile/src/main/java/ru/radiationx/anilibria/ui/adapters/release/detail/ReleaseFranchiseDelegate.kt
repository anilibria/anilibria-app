package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseFranchiseBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseFranchiseListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.shared_app.imageloader.showImageUrl

class ReleaseFranchiseDelegate(
    private val itemListener: (ReleaseId) -> Unit
) : AppAdapterDelegate<ReleaseFranchiseListItem, ListItem, ReleaseFranchiseDelegate.ViewHolder>(
    R.layout.item_release_franchise,
    { it is ReleaseFranchiseListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseFranchiseListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val itemListener: (ReleaseId) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseFranchiseBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: ReleaseFranchiseListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemImage.showImageUrl(item.state.poster)
            binding.itemName.text = item.state.title
            binding.itemDesc.text = item.state.subtitle
            binding.itemIndicator.isVisible = item.state.selected

            binding.root.setOnClickListener {
                itemListener.invoke(item.state.id)
            }
            binding.root.isClickable = !item.state.selected
        }
    }

}