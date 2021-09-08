package ru.radiationx.anilibria.presentation.favorites

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.anilibria.ui.fragments.release.list.ReleaseScreenState
import ru.radiationx.data.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 13.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FavoritesView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state:ReleaseScreenState)
}