package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.text.Html
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationSectionBinding
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
        itemView: View,
        private val linkClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationSectionBinding>()

        init {
            binding.tvSubtitle.movementMethod = LinkMovementMethod {
                linkClickListener.invoke(it)
                true
            }
        }

        fun bind(divider: DonationContentSection) {
            binding.tvTitle.text = divider.title
            binding.tvSubtitle.text = divider.subtitle?.let { Html.fromHtml(it) }
            binding.tvTitle.isVisible = divider.title != null
            binding.tvSubtitle.isVisible = divider.subtitle != null
        }
    }
}