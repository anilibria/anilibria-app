package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation_caption.*
import ru.radiationx.anilibria.R
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
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: DonationContentHeader) {
            tvText.text = data.text
        }
    }
}