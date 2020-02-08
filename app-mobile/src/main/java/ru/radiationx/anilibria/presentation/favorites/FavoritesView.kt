package ru.radiationx.anilibria.presentation.favorites

import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.common.IBaseView

/**
 * Created by radiationx on 13.01.18.
 */
interface FavoritesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun removeReleases(releases: List<ReleaseItem>)

    fun setEndless(enable: Boolean)
}