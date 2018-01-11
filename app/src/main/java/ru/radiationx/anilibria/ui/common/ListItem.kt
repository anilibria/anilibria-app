package ru.radiationx.anilibria.ui.common

import ru.radiationx.anilibria.entity.app.OtherMenuItem

sealed class ListItem

class ProfileListItem : ListItem()
class MenuListItem(val menuItem: OtherMenuItem) : ListItem()
class DividerShadowListItem() : ListItem()
