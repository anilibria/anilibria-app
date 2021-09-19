package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donations_card.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.DonationCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class DonationCardDelegate(
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : AppAdapterDelegate<DonationCardListItem, ListItem, DonationCardDelegate.ViewHolder>(
    R.layout.item_donations_card,
    { it is DonationCardListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: DonationCardListItem, holder: ViewHolder) =
        holder.bind(item.state)

    class ViewHolder(
        override val containerView: View,
        private val clickListener: (DonationCardItemState) -> Unit,
        private val closeClickListener: (DonationCardItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: DonationCardItemState) {
            tvTitle.text = state.title
            tvSubtitle.text = state.subtitle
            tvSubtitle.isVisible = state.subtitle != null
            btClose.isVisible = state.canClose

            containerView.setOnClickListener { clickListener.invoke(state) }
            btClose.setOnClickListener { closeClickListener.invoke(state) }
        }
    }
}