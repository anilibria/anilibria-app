package ru.radiationx.anilibria.ui.adapters.other

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_other_menu.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.extension.setCompatDrawable
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class MenuItemDelegate(
        private val clickListener: (OtherMenuItem) -> Unit
) : AppAdapterDelegate<MenuListItem, ListItem, MenuItemDelegate.ViewHolder>(
        R.layout.item_other_menu,
        { it is MenuListItem },
        { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: MenuListItem, holder: ViewHolder) = holder.bind(item.menuItem)

    class ViewHolder(
            override val containerView: View,
            val clickListener: (OtherMenuItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var item: OtherMenuItem

        init {
            containerView.setOnClickListener { clickListener(item) }
        }

        fun bind(menuItem: OtherMenuItem) {
            this.item = menuItem
            otherMenuTitle.text = menuItem.title
            otherMenuIcon.setCompatDrawable(menuItem.icon)
        }
    }
}
