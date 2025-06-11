package ru.radiationx.anilibria.ui.adapters.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemBottomTabBinding
import ru.radiationx.anilibria.ui.activities.main.MainTab
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr

class BottomTabDelegate(
    private val clickListener: (MainTab) -> Unit,
    private val longClickListener: (MainTab) -> Unit
) : AppAdapterDelegate<BottomTabListItem, ListItem, BottomTabDelegate.ViewHolder>(
    R.layout.item_bottom_tab,
    { it is BottomTabListItem },
    { ViewHolder(it, clickListener, longClickListener) }
) {

    override fun bindData(item: BottomTabListItem, holder: ViewHolder) =
        holder.bind(item.tab, item.selected)

    class ViewHolder(
        itemView: View,
        private val clickListener: (MainTab) -> Unit,
        private val longClickListener: (MainTab) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemBottomTabBinding>()

        fun bind(tab: MainTab, selected: Boolean) {
            val colorRes = if (selected) {
                com.google.android.material.R.attr.colorSecondaryVariant
            } else {
                com.google.android.material.R.attr.colorOnBackground
            }
            val icRes = when (tab) {
                MainTab.Feed -> R.drawable.ic_newspaper
                MainTab.Favorites -> R.drawable.ic_star
                MainTab.Catalog -> R.drawable.ic_toolbar_search
                MainTab.Collections -> R.drawable.ic_collections
                MainTab.YouTube -> R.drawable.ic_youtube
                MainTab.Other -> R.drawable.ic_other
            }
            binding.tabIcon.setCompatDrawable(icRes)
            binding.tabIcon.setTintColorAttr(colorRes)
            binding.root.setOnClickListener { clickListener.invoke(tab) }
            binding.root.setOnLongClickListener {
                longClickListener.invoke(tab)
                true
            }
        }
    }
}
