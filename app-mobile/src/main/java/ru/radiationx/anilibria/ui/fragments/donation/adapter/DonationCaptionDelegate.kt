package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.view.View
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemDonationCaptionBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.app.donation.models.DonationContentCaption
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.toAbsoluteUrl

class DonationCaptionDelegate(
    private val linkClickListener: (Url.Absolute) -> Unit,
) : AppAdapterDelegate<DonationCaptionListItem, ListItem, DonationCaptionDelegate.ViewHolder>(
    R.layout.item_donation_caption,
    { it is DonationCaptionListItem },
    { ViewHolder(it, linkClickListener) }
) {

    override fun bindData(item: DonationCaptionListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
        private val linkClickListener: (Url.Absolute) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemDonationCaptionBinding>()

        init {
            binding.tvText.movementMethod = LinkMovementMethod {
                linkClickListener.invoke(it.toAbsoluteUrl())
                true
            }
        }

        fun bind(data: DonationContentCaption) {
            binding.tvText.text = data.text.parseAsHtml()
        }
    }
}