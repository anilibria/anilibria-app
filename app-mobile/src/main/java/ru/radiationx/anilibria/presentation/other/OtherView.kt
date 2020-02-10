package ru.radiationx.anilibria.presentation.other

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.other.OtherMenuItem
import ru.radiationx.data.entity.app.other.ProfileItem

@StateStrategyType(AddToEndSingleStrategy::class)
interface OtherView : IBaseView {
    fun showItems(profileItem: ProfileItem, menu: List<MutableList<OtherMenuItem>>)
    fun updateProfile()
}
