package ru.radiationx.anilibria.ui.fragments.other.adapter

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
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

    fun addProfile(profileItem: ProfileItem) {
        items.add(ProfileListItem(profileItem))
        items.add(DividerShadowListItem())
    }

    fun addMenu(newItems: MutableList<OtherMenuItem>) {
        items.addAll(newItems.map { MenuListItem(it) })
        items.add(DividerShadowListItem())
    }
}
