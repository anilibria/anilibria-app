package ru.radiationx.anilibria.presentation.favorites

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView
import ru.radiationx.data.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 13.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FavoritesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun removeReleases(releases: List<ReleaseItem>)

    fun setEndless(enable: Boolean)
}