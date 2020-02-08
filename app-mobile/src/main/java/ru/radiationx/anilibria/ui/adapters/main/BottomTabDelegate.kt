package ru.radiationx.anilibria.ui.adapters.main

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_bottom_tab.*
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class BottomTabDelegate(private val clickListener: Listener) : AppAdapterDelegate<BottomTabListItem, ListItem, BottomTabDelegate.ViewHolder>(
        R.layout.item_bottom_tab,
        { it is BottomTabListItem },
        { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: BottomTabListItem, holder: ViewHolder) =
            holder.bind(item.item, item.selected)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: MainActivity.Tab

        init {
            containerView.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: MainActivity.Tab, selected: Boolean) {
            this.currentItem = item
            tabIcon.setCompatDrawable(item.icon)
            val colorRes = if (selected) R.attr.tab_color_checked else R.attr.tab_color_unchecked
            tabIcon.setTintColorAttr(colorRes)
        }
    }

    interface Listener {
        fun onTabClick(tab: MainActivity.Tab)
    }
}
