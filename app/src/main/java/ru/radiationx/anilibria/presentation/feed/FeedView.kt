package ru.radiationx.anilibria.presentation.feed

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.presentation.common.IBaseView

/* Created by radiationx on 16.11.17. */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FeedView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showReleases(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(releases: List<ReleaseItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun updateReleases(releases: List<ReleaseItem>)

    fun setEndless(enable: Boolean)
}
