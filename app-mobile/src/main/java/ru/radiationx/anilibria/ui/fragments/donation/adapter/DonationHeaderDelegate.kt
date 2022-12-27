package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationHeaderBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.donation.DonationContentHeader

class DonationHeaderDelegate :
    AppAdapterDelegate<DonationHeaderListItem, ListItem, DonationHeaderDelegate.ViewHolder>(
        R.layout.item_donation_header,
        { it is DonationHeaderListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: DonationHeaderListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationHeaderBinding>()

        fun bind(data: DonationContentHeader) {
            binding.tvText.text = data.text
        }
    }
}