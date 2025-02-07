package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationCardBinding
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.DonationCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 13.01.18.
 */
class DonationCardDelegate(
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : AppAdapterDelegate<DonationCardListItem, ListItem, DonationCardDelegate.ViewHolder>(
    R.layout.item_donation_card,
    { it is DonationCardListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: DonationCardListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (DonationCardItemState) -> Unit,
        private val closeClickListener: (DonationCardItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationCardBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(item: DonationCardListItem) {
            dimensionsApplier.applyMargins(Side.Left, Side.Right)
            DonationInfoBinder.bind(
                item.state,
                binding.donationInfo,
                clickListener,
                closeClickListener
            )
        }
    }
}