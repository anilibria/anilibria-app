package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationDividerBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.donation.DonationContentDivider
import kotlin.math.roundToInt

class DonationDividerDelegate :
    AppAdapterDelegate<DonationDividerListItem, ListItem, DonationDividerDelegate.ViewHolder>(
        R.layout.item_donation_divider,
        { it is DonationDividerListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: DonationDividerListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationDividerBinding>()

        fun bind(data: DonationContentDivider) {
            binding.space.updateLayoutParams {
                height = (data.height * binding.space.resources.displayMetrics.density).roundToInt()
            }
        }
    }
}