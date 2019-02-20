package ru.radiationx.anilibria.ui.adapters.auth

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_social_auth.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import java.util.regex.Pattern

class SocialAuthDelegate(
        private val clickListener: (SocialAuth) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is SocialAuthListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        (items[position] as SocialAuthListItem).also {
            (holder as ViewHolder).bind(it.item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_social_auth, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: SocialAuth

        init {
            view.itemSocialBtn.setOnClickListener { clickListener.invoke(currentItem) }
        }

        fun bind(item: SocialAuth) {
            currentItem = item
            view.apply {
                val icon = when (item.key) {
                    SocialAuth.KEY_VK -> ContextCompat.getDrawable(context, R.drawable.ic_logo_vk)
                    SocialAuth.KEY_PATREON -> ContextCompat.getDrawable(context, R.drawable.ic_logo_patreon)
                    else -> null
                }

                val color = when (item.key) {
                    SocialAuth.KEY_VK -> ContextCompat.getColor(context, R.color.brand_vk)
                    SocialAuth.KEY_PATREON -> ContextCompat.getColor(context, R.color.brand_patreon)
                    else -> context.getColorFromAttr(R.attr.textDefault)
                }

                itemSocialBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
                itemSocialBtn.setTextColor(color)
                itemSocialBtn.text = item.title
            }
        }
    }
}