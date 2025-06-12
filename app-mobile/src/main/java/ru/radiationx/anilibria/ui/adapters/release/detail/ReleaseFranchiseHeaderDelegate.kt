package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseFranchiseHeaderBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseFranchiseHeaderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseFranchiseListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

class ReleaseFranchiseHeaderDelegate(

) : AppAdapterDelegate<ReleaseFranchiseHeaderListItem, ListItem, ReleaseFranchiseHeaderDelegate.ViewHolder>(
    R.layout.item_release_franchise_header,
    { it is ReleaseFranchiseHeaderListItem },
    { ViewHolder(it) }
) {

    override fun bindData(item: ReleaseFranchiseHeaderListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseFranchiseHeaderBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: ReleaseFranchiseHeaderListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemName.text = item.state.title
            binding.itemDesc.text = item.state.subtitle
        }
    }

}