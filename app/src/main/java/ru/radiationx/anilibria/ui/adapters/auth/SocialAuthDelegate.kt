package ru.radiationx.anilibria.ui.adapters.auth

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_social_auth.*
import kotlinx.android.synthetic.main.item_social_auth.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class SocialAuthDelegate(
        private val clickListener: (SocialAuth) -> Unit
) : AppAdapterDelegate<SocialAuthListItem, ListItem, SocialAuthDelegate.ViewHolder>(
        R.layout.item_social_auth,
        { it is SocialAuthListItem },
        { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: SocialAuthListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (SocialAuth) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: SocialAuth

        init {
            itemSocialBtn.setOnClickListener { clickListener.invoke(currentItem) }
        }

        fun bind(item: SocialAuth) {
            currentItem = item
            val icon = when (item.key) {
                SocialAuth.KEY_VK -> itemSocialBtn.getCompatDrawable(R.drawable.ic_logo_vk)
                SocialAuth.KEY_PATREON -> itemSocialBtn.getCompatDrawable(R.drawable.ic_logo_patreon)
                else -> null
            }

            val color = when (item.key) {
                SocialAuth.KEY_VK -> itemSocialBtn.getCompatColor(R.color.brand_vk)
                SocialAuth.KEY_PATREON -> itemSocialBtn.getCompatColor(R.color.brand_patreon)
                else -> itemSocialBtn.context.getColorFromAttr(R.attr.textDefault)
            }

            itemSocialBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            itemSocialBtn.setTextColor(color)
            itemSocialBtn.text = item.title
        }
    }
}