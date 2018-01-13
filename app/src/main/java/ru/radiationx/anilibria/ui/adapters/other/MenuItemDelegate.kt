package ru.radiationx.anilibria.ui.adapters.other

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_other_menu.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem

class MenuItemDelegate(private val clickListener: (OtherMenuItem) -> Unit) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is MenuListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as MenuListItem
        (holder as ViewHolder).bind(item.menuItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_other_menu, parent, false),
            clickListener
    )

    private class ViewHolder(
            val view: View,
            val clickListener: (OtherMenuItem) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var item: OtherMenuItem

        init {
            view.setOnClickListener { clickListener(item) }
        }

        fun bind(menuItem: OtherMenuItem) {
            this.item = menuItem
            view.run {
                otherMenuTitle.text = menuItem.title
                otherMenuIcon.setImageDrawable(AppCompatResources.getDrawable(view.context, menuItem.icon))
            }
        }
    }
}
