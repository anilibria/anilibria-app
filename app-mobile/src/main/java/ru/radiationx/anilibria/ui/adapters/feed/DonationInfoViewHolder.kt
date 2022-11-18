package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.databinding.ViewDonationInfoBinding
import ru.radiationx.anilibria.model.DonationCardItemState

class DonationInfoViewHolder(
    itemView: View,
    private val infoBinding: ViewDonationInfoBinding,
    private val clickListener: (DonationCardItemState) -> Unit,
    private val closeClickListener: (DonationCardItemState) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(state: DonationCardItemState) {
        infoBinding.tvTitle.text = state.title
        infoBinding.tvSubtitle.text = state.subtitle
        infoBinding.tvTitle.isVisible = state.title != null
        infoBinding.tvSubtitle.isVisible = state.subtitle != null
        infoBinding.btClose.isVisible = state.canClose

        infoBinding.root.setOnClickListener { clickListener.invoke(state) }
        infoBinding.btClose.setOnClickListener { closeClickListener.invoke(state) }
    }
}