package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_donation_info.*
import ru.radiationx.anilibria.model.DonationCardItemState

class DonationInfoViewHolder(
    override val containerView: View,
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(state: DonationCardItemState) {
        tvTitle.text = state.title
        tvSubtitle.text = state.subtitle
        tvTitle.isVisible = state.title != null
        tvSubtitle.isVisible = state.subtitle != null
        btClose.isVisible = state.canClose

        containerView.setOnClickListener { clickListener.invoke(state) }
        btClose.setOnClickListener { closeClickListener.invoke(state) }
    }
}