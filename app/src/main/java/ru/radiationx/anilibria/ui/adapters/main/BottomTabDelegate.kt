package ru.radiationx.anilibria.ui.adapters.main

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_bottom_tab.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem

class BottomTabDelegate(private val clickListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is BottomTabListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as BottomTabListItem
        (holder as ViewHolder).bind(item.item, item.selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_tab, parent, false))

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: MainActivity.Tab

        init {
            view.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: MainActivity.Tab, selected: Boolean) {
            this.currentItem = item
            view.run {
                tabIcon.setImageDrawable(ContextCompat.getDrawable(context, item.icon))
                val colorRes = if (selected) R.color.tab_color_checked else R.color.tab_color_unchecked
                tabIcon.setColorFilter(
                        ContextCompat.getColor(context, colorRes),
                        PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    interface Listener {
        fun onTabClick(tab: MainActivity.Tab)
    }
}
