package ru.radiationx.anilibria.ui.fragments.donation.adapter

import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation_caption.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.entity.domain.donation.DonationContentCaption

class DonationCaptionDelegate(
    private val linkClickListener: (String) -> Unit
) : AppAdapterDelegate<DonationCaptionListItem, ListItem, DonationCaptionDelegate.ViewHolder>(
    R.layout.item_donation_caption,
    { it is DonationCaptionListItem },
    { ViewHolder(it, linkClickListener) }
) {

    override fun bindData(item: DonationCaptionListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        override val containerView: View,
        private val linkClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            tvText.movementMethod = LinkMovementMethod {
                linkClickListener.invoke(it)
                true
            }
        }

        fun bind(data: DonationContentCaption) {
            tvText.text = data.text.let { Html.fromHtml(it) }
        }
    }
}