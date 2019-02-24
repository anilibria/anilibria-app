package ru.radiationx.anilibria.ui.adapters.main

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_bottom_tab.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class BottomTabDelegate(private val clickListener: Listener) : AppAdapterDelegate<BottomTabListItem, ListItem, BottomTabDelegate.ViewHolder>(
        R.layout.item_bottom_tab,
        { it is BottomTabListItem },
        { ViewHolder(it, clickListener) }
)  {

    override fun bindData(item: BottomTabListItem, holder: ViewHolder) =
            holder.bind(item.item, item.selected)

    class ViewHolder(
            val view: View,
            private val clickListener: Listener
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: MainActivity.Tab

        init {
            view.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: MainActivity.Tab, selected: Boolean) {
            this.currentItem = item
            view.run {
                tabIcon.setImageDrawable(ContextCompat.getDrawable(context, item.icon))
                val colorRes = if (selected) R.attr.tab_color_checked else R.attr.tab_color_unchecked
                tabIcon.setColorFilter(
                        context.getColorFromAttr(colorRes),
                        PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    interface Listener {
        fun onTabClick(tab: MainActivity.Tab)
    }
}
