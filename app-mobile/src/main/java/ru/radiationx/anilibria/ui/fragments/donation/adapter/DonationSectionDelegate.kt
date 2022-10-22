package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.text.Html
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation_section.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.entity.domain.donation.DonationContentSection

class DonationSectionDelegate(
    private val linkClickListener: (String) -> Unit
) : AppAdapterDelegate<DonationSectionListItem, ListItem, DonationSectionDelegate.ViewHolder>(
    R.layout.item_donation_section,
    { it is DonationSectionListItem },
    { ViewHolder(it, linkClickListener) }
) {

    override fun bindData(item: DonationSectionListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        override val containerView: View,
        private val linkClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            tvSubtitle.movementMethod = LinkMovementMethod {
                linkClickListener.invoke(it)
                true
            }
        }

        fun bind(divider: DonationContentSection) {
            tvTitle.text = divider.title
            tvSubtitle.text = divider.subtitle?.let { Html.fromHtml(it) }
            tvTitle.isVisible = divider.title != null
            tvSubtitle.isVisible = divider.subtitle != null
        }
    }
}