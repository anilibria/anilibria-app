package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseDonateBinding
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseDonateListItem
import ru.radiationx.anilibria.ui.adapters.feed.DonationInfoBinder
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseDonateDelegate(
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : AppAdapterDelegate<ReleaseDonateListItem, ListItem, ReleaseDonateDelegate.ViewHolder>(
    R.layout.item_release_donate,
    { it is ReleaseDonateListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: ReleaseDonateListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (DonationCardItemState) -> Unit,
        private val closeClickListener: (DonationCardItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseDonateBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: ReleaseDonateListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            DonationInfoBinder.bind(
                item.state,
                binding.donationInfo,
                clickListener,
                closeClickListener
            )
        }
    }
}