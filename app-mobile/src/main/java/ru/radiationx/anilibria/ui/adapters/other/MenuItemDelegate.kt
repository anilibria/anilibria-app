package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_other_menu.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.shared.ktx.android.setCompatDrawable

class MenuItemDelegate(
    private val clickListener: (OtherMenuItemState) -> Unit
) : AppAdapterDelegate<MenuListItem, ListItem, MenuItemDelegate.ViewHolder>(
    R.layout.item_other_menu,
    { it is MenuListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: MenuListItem, holder: ViewHolder) = holder.bind(item.menuItem)

    class ViewHolder(
        override val containerView: View,
        val clickListener: (OtherMenuItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: OtherMenuItemState) {
            otherMenuTitle.text = state.title
            otherMenuIcon.setCompatDrawable(state.iconRes)
            containerView.setOnClickListener { clickListener(state) }
        }
    }
}
