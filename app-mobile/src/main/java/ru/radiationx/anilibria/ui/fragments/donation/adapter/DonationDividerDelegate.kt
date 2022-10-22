package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation_divider.*
import ru.radiationx.anilibria.R
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
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: DonationContentDivider) {
            space.updateLayoutParams {
                height = (data.height * space.resources.displayMetrics.density).roundToInt()
            }
        }
    }
}