package ru.radiationx.anilibria.ui.adapters.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemBottomTabBinding
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr

class BottomTabDelegate(private val clickListener: Listener) :
    AppAdapterDelegate<BottomTabListItem, ListItem, BottomTabDelegate.ViewHolder>(
        R.layout.item_bottom_tab,
        { it is BottomTabListItem },
        { ViewHolder(it, clickListener) }
    ) {

    override fun bindData(item: BottomTabListItem, holder: ViewHolder) =
        holder.bind(item.item, item.selected)

    class ViewHolder(
        itemView: View,
        private val clickListener: Listener
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemBottomTabBinding>()

        fun bind(item: MainActivity.Tab, selected: Boolean) {
            val colorRes = if (selected) R.attr.colorSecondaryVariant else R.attr.colorOnBackground
            binding.tabIcon.setCompatDrawable(item.icon)
            binding.tabIcon.setTintColorAttr(colorRes)
            binding.root.setOnClickListener { clickListener.onTabClick(item) }
        }
    }

    interface Listener {
        fun onTabClick(tab: MainActivity.Tab)
    }
}
