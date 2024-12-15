package ru.radiationx.anilibria.ui.adapters.feed

import androidx.core.view.isVisible
import ru.radiationx.anilibria.databinding.ViewDonationInfoBinding
import ru.radiationx.anilibria.model.DonationCardItemState

object DonationInfoBinder {
    fun bind(
        state: DonationCardItemState,
        infoBinding: ViewDonationInfoBinding,
        clickListener: (DonationCardItemState) -> Unit,
        closeClickListener: (DonationCardItemState) -> Unit
    ) {
        infoBinding.tvTitle.text = state.title
        infoBinding.tvSubtitle.text = state.subtitle
        infoBinding.tvSubtitle.isVisible = state.subtitle != null
        infoBinding.btClose.isVisible = state.canClose

        infoBinding.root.setOnClickListener { clickListener.invoke(state) }
        infoBinding.btClose.setOnClickListener { closeClickListener.invoke(state) }
    }
}