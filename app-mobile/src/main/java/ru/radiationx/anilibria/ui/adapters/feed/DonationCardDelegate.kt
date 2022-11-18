package ru.radiationx.anilibria.ui.adapters.feed

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationCardBinding
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.DonationCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class DonationCardDelegate(
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : AppAdapterDelegate<DonationCardListItem, ListItem, DonationInfoViewHolder>(
    R.layout.item_donation_card,
    { it is DonationCardListItem },
    {
        val binding = ItemDonationCardBinding.bind(it)
        DonationInfoViewHolder(it, binding.donationInfo, clickListener, closeClickListener)
    }
) {

    override fun bindData(item: DonationCardListItem, holder: DonationInfoViewHolder) =
        holder.bind(item.state)
}