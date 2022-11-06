package ru.radiationx.anilibria.ui.adapters.auth

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_social_auth.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable
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
        override val containerView: View,
        private val clickListener: (SocialAuthItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {


        fun bind(state: SocialAuthItemState) {
            val icon = state.iconRes?.let { itemSocialBtn.getCompatDrawable(it) }

            val color = state.colorRes?.let { itemSocialBtn.getCompatColor(it) }
                ?: itemSocialBtn.context.getColorFromAttr(R.attr.textDefault)

            itemSocialBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            itemSocialBtn.setTextColor(color)

            itemSocialBtn.text = state.title
            itemSocialBtn.setOnClickListener { clickListener.invoke(state) }
        }
    }
}