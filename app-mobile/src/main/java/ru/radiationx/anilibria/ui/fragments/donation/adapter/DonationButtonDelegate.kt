package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.content.res.ColorStateList
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationButtonBinding
import ru.radiationx.anilibria.model.asDataColorRes
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.getCompatDrawable

class DonationButtonDelegate(
    private val clickListener: (DonationContentButton) -> Unit
) : AppAdapterDelegate<DonationButtonListItem, ListItem, DonationButtonDelegate.ViewHolder>(
    R.layout.item_donation_button,
    { it is DonationButtonListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: DonationButtonListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
        private val clickListener: (DonationContentButton) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationButtonBinding>()

        fun bind(data: DonationContentButton) {
            binding.btAction.text = data.text
            binding.btAction.setOnClickListener { clickListener.invoke(data) }

            val textColor = data.brand?.asDataColorRes()
                ?.let { binding.btAction.getCompatColor(it) }
                ?: binding.btAction.context.getColorFromAttr(R.attr.colorAccent)

            binding.btAction.icon =
                data.icon?.asDataIconRes()?.let { binding.btAction.getCompatDrawable(it) }
            binding.btAction.setTextColor(textColor)
            binding.btAction.iconTint = ColorStateList.valueOf(textColor)
        }
    }
}