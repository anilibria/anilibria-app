package ru.radiationx.anilibria.ui.adapters.auth

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemSocialAuthBinding
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class SocialAuthDelegate(
    private val clickListener: (SocialAuthItemState) -> Unit
) : AppAdapterDelegate<SocialAuthListItem, ListItem, SocialAuthDelegate.ViewHolder>(
    R.layout.item_social_auth,
    { it is SocialAuthListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: SocialAuthListItem, holder: ViewHolder) = holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (SocialAuthItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemSocialAuthBinding>()

        fun bind(state: SocialAuthItemState) {
            val icon = state.iconRes?.let { binding.itemSocialBtn.getCompatDrawable(it) }

            val color = state.colorRes
                ?.let { binding.itemSocialBtn.getCompatColor(it) }
                ?: binding.itemSocialBtn.context.getColorFromAttr(R.attr.textDefault)

            binding.itemSocialBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                null,
                null,
                null
            )
            binding.itemSocialBtn.setTextColor(color)

            binding.itemSocialBtn.text = state.title
            binding.itemSocialBtn.setOnClickListener { clickListener.invoke(state) }
        }
    }
}