package ru.radiationx.anilibria.ui.fragments.other.adapter

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_other_menu.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.OtherMenuItem
import ru.radiationx.anilibria.ui.common.ListItem
import ru.radiationx.anilibria.ui.common.MenuListItem

class MenuItemDelegate : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is MenuListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as MenuListItem
        (holder as ViewHolder).bind(item.menuItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_menu, parent, false))

    private class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(menuItem: OtherMenuItem) {
            view.run {
                otherMenuTitle.text = menuItem.title
                otherMenuIcon.setImageDrawable(AppCompatResources.getDrawable(view.context, menuItem.icon))
            }
        }
    }
}
