package ru.radiationx.anilibria.ui.adapters.release.detail

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseDonateBinding
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseDonateListItem
import ru.radiationx.anilibria.ui.adapters.feed.DonationInfoViewHolder
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseDonateDelegate(
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : AppAdapterDelegate<ReleaseDonateListItem, ListItem, DonationInfoViewHolder>(
    R.layout.item_release_donate,
    { it is ReleaseDonateListItem },
    {
        val binding = ItemReleaseDonateBinding.bind(it)
        DonationInfoViewHolder(it, binding.donationInfo, clickListener, closeClickListener)
    }
) {

    override fun bindData(item: ReleaseDonateListItem, holder: DonationInfoViewHolder) =
        holder.bind(item.state)
}