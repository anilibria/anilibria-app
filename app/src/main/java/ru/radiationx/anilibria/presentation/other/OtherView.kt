package ru.radiationx.anilibria.presentation.other

import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

interface OtherView : IBaseView {
    fun showItems(profileItem: ProfileItem, menu: List<MutableList<OtherMenuItem>>)
    fun updateProfile()
}
