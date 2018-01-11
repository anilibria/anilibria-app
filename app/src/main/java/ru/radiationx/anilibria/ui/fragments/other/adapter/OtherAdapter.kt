package ru.radiationx.anilibria.ui.fragments.other.adapter

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.OtherMenuItem
import ru.radiationx.anilibria.ui.common.DividerShadowListItem
import ru.radiationx.anilibria.ui.common.ListItem
import ru.radiationx.anilibria.ui.common.MenuListItem
import ru.radiationx.anilibria.ui.common.ProfileListItem

class OtherAdapter : ListDelegationAdapter<MutableList<ListItem>>() {
    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ProfileItemDelegate())
            addDelegate(DividerShadowItemDelegate())
            addDelegate(MenuItemDelegate())
        }
    }

    fun clear() {
        items.clear()
    }

    fun addProfile(): OtherAdapter {
        items.add(ProfileListItem())
        items.add(DividerShadowListItem())
        return this
    }

    fun addMenu(newItems: MutableList<OtherMenuItem>): OtherAdapter {
        items.addAll(newItems.map { MenuListItem(it) })
        items.add(DividerShadowListItem())
        return this
    }
}
