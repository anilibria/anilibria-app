package ru.radiationx.anilibria.presentation.other

import ru.radiationx.data.entity.app.other.OtherMenuItem
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.anilibria.presentation.common.IBaseView

interface OtherView : IBaseView {
    fun showItems(profileItem: ProfileItem, menu: List<MutableList<OtherMenuItem>>)
    fun updateProfile()
}
